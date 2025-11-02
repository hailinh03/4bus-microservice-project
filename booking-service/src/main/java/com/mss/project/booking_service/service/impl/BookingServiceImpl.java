package com.mss.project.booking_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mss.project.booking_service.entities.Payment;
import com.mss.project.booking_service.enums.PaymentStatus;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.booking.BookingRequest;
import com.mss.project.booking_service.payload.booking.BookingResponse;
import com.mss.project.booking_service.payload.booking.Seat;
import com.mss.project.booking_service.payload.payment.PaymentLinkRequest;
import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.exception.BookingException;
import com.mss.project.booking_service.payload.trip.TripDTO;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.repository.PaymentRepository;
import com.mss.project.booking_service.service.BookingService;
import com.mss.project.booking_service.service.PaymentService;
import com.mss.project.booking_service.service.TicketService;
import com.mss.project.booking_service.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;

import com.mss.project.booking_service.payload.booking.BookingHistoryListRequest;
import com.mss.project.booking_service.payload.booking.BookingHistoryResponse;
import com.mss.project.booking_service.payload.ticket.TicketResponse;
import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.mapper.TicketMapper;
import com.mss.project.booking_service.repository.TicketRepository;
import com.mss.project.booking_service.exception.BookingHistoryException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final TripService tripService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public BookingResponse initiateBooking(BookingRequest bookingRequest) {
        try {
            List<String> bookedSeatIds = ticketService.getBookedSeatsByTripId(bookingRequest.getTripId());
            List<String> requestedSeatIds = bookingRequest.getSeats().stream()
                    .map(Seat::getSeatId)
                    .toList();
            // Check if any requested seat is already booked
            for (String seatId : requestedSeatIds) {
                if (bookedSeatIds.contains(seatId)) {
                    throw new BookingException("Seat " + seatId + " is already booked.");
                }
            }
            // 1. Extract user information from security context
            Long userId = extractUserIdFromSecurityContext();

            // 2. Fetch and validate trip details
            TripDTO tripDetails = fetchAndValidateTrip(bookingRequest.getTripId());

            // 3. Calculate total price for selected seats
            double totalPrice = calculateTotalPrice(tripDetails, requestedSeatIds);

            // 4. Create and save booking
            Booking savedBooking = createAndSaveBooking(bookingRequest, userId, totalPrice);

            // 5. Create payment link
            CheckoutResponseData paymentData = createPaymentLink(savedBooking);

            // 6. Build and return response
            return buildBookingResponse(savedBooking, paymentData);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            throw new BookingException("Failed to initiate booking: " + e.getMessage(), e);
        }
    }

    private Long extractUserIdFromSecurityContext() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            return Long.parseLong(context.getAuthentication().getName());
        } catch (Exception e) {
            throw new BookingException("Failed to extract user information from security context", e);
        }
    }

    private TripDTO fetchAndValidateTrip(Integer tripId) {
        try {
            ApiResponse<TripDTO> tripApiResponse = tripService.getTripById(tripId);
            TripDTO tripDetails = tripApiResponse.getData();

            if (tripDetails == null) {
                throw new BookingException("Trip not found with id: " + tripId);
            }

            return tripDetails;
        } catch (Exception e) {
            if (e instanceof BookingException) {
                throw e;
            }
            throw new BookingException("Failed to fetch trip details for id: " + tripId, e);
        }
    }

    private double calculateTotalPrice(TripDTO tripDetails, List<String> selectedSeatIds) {
        try {
            // Check if seat codes are available
            if (!isSeatCodesAvailable(tripDetails)) {
                return calculateDefaultPrice(selectedSeatIds);
            }

            String seatCodesJson = tripDetails.getBus().getCategory().getSeatCodes();
            return calculatePriceFromSeatCodes(seatCodesJson, selectedSeatIds);

        } catch (Exception e) {
            System.out.println(
                    "Warning: Failed to calculate price from seat codes, using default pricing: " + e.getMessage());
            return calculateDefaultPrice(selectedSeatIds);
        }
    }

    private boolean isSeatCodesAvailable(TripDTO tripDetails) {
        return tripDetails.getBus() != null &&
                tripDetails.getBus().getCategory() != null &&
                tripDetails.getBus().getCategory().getSeatCodes() != null;
    }

    private double calculateDefaultPrice(List<String> selectedSeatCodes) {
        final double DEFAULT_PRICE_PER_SEAT = 100000.0;
        return selectedSeatCodes.size() * DEFAULT_PRICE_PER_SEAT;
    }

    private double calculatePriceFromSeatCodes(String seatCodesJson, List<String> selectedSeatIds)
            throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode seatCodesNode = objectMapper.readTree(seatCodesJson);
            JsonNode floors = seatCodesNode.path("floors");

            double totalPrice = 0.0;

            for (JsonNode floor : floors) {
                JsonNode seats = floor.path("seats");
                for (JsonNode seat : seats) {
                    String seatId = seat.path("id").asText();
                    if (selectedSeatIds.contains(seatId)) {
                        double seatPrice = seat.path("price").asDouble(0.0);
                        totalPrice += seatPrice;
                    }
                }
            }

            if (totalPrice == 0.0) {
                throw new BookingException("No valid seats found for the selected seat codes: " + selectedSeatIds);
            }

            return totalPrice;
        } catch (Exception e) {
            if (e instanceof BookingException) {
                throw e;
            }
            throw new RuntimeException("Failed to parse seat codes JSON", e);
        }
    }

    private Booking createAndSaveBooking(BookingRequest request, Long userId, double totalPrice) {
        try {
            Booking booking = Booking.builder()
                    .tripId(request.getTripId())
                    .seatCodes(String.join(",", request.getSeats().stream() // lấy list seats gồm các seatCode và seatId
                            .map(seat -> seat.getSeatId() + ":" + seat.getSeatCode() + ":" + seat.getPrice()) // lấy
                                                                                                              // seatId:seatCode
                            .toList()))
                    .numberOfTickets(request.getSeats().size())
                    .totalPrice(totalPrice)
                    .status(BookingStatus.PENDING)
                    .userId(userId)
                    .build();

            return bookingRepository.save(booking);
        } catch (Exception e) {
            throw new BookingException("Failed to save booking to database", e);
        }
    }

    private CheckoutResponseData createPaymentLink(Booking booking) {
        try {
            PaymentLinkRequest paymentRequest = new PaymentLinkRequest();
            paymentRequest.setAmount(booking.getTotalPrice().intValue());
            paymentRequest.setDescription(
                    "Đặt chỗ ngồi " + booking.getSeatCodes() + " trên chuyến xe " + booking.getTripId());
            paymentRequest.setItems(Collections.singletonList(
                    ItemData.builder()
                            .name("Đặt chỗ ngồi " + booking.getSeatCodes() + " trên chuyến xe " + booking.getTripId())
                            .price(booking.getTotalPrice().intValue())
                            .quantity(1)
                            .build()));
            CheckoutResponseData checkoutResponse = paymentService.createPaymentLink(paymentRequest);

            booking.setOrderCode(checkoutResponse.getOrderCode());
            bookingRepository.save(booking);

            Payment payment = Payment.builder()
                    .id(checkoutResponse.getOrderCode())
                    .amount(checkoutResponse.getAmount())
                    .description("ORD" + checkoutResponse.getOrderCode())
                    .status(PaymentStatus.PENDING)
                    .booking(booking)
                    .build();
            paymentRepository.save(payment);

            return checkoutResponse;

        } catch (Exception e) {
            System.err.println("Warning: Failed to create payment link: " + e.getMessage());
            return null; // Return null if payment link creation fails
        }
    }

    private BookingResponse buildBookingResponse(Booking booking, CheckoutResponseData paymentResponse) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingId(booking.getId()) // For backward compatibility
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .numberOfTickets(booking.getNumberOfTickets())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .seatCodes(booking.getSeatCodes())
                .orderCode(booking.getOrderCode())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .paymentResponse(paymentResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingHistoryResponse> getMyBookingHistory(BookingHistoryListRequest request) {
        try {
            Long userId = extractUserIdFromSecurityContext();
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            Page<Booking> bookings = bookingRepository.findBookingHistoryByUserId(
                    userId,
                    request.getStatus(),
                    request.getTripId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    pageable);
            if (bookings.isEmpty()) {
                return Page.empty(pageable);
            }
            // Bulk load tickets for all bookings to avoid N+1 queries
            List<Integer> bookingIds = bookings.getContent().stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList());
            List<Ticket> tickets = ticketRepository.findByBookingIds(bookingIds);
            Map<Integer, List<Ticket>> ticketsByBookingId = tickets.stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getBooking().getId()));
            // Bulk load trip data for all unique trip IDs
            Map<Integer, TripDTO> tripMap = loadTripsForBookings(bookings.getContent());
            return bookings.map(booking -> mapToBookingHistoryResponse(booking, ticketsByBookingId, tripMap));
        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            throw new BookingHistoryException("Failed to fetch booking history: " + e.getMessage());
        }
    }

    private Map<Integer, TripDTO> loadTripsForBookings(List<Booking> bookings) {
        try {
            List<Integer> tripIds = bookings.stream()
                    .map(Booking::getTripId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, TripDTO> tripMap = tripIds.stream()
                    .collect(Collectors.toMap(
                            tripId -> tripId,
                            tripId -> {
                                try {
                                    ApiResponse<TripDTO> tripApiResponse = tripService.getTripById(tripId);
                                    return tripApiResponse.getData();
                                } catch (Exception e) {
                                    return null;
                                }
                            }));
            return tripMap;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private BookingHistoryResponse mapToBookingHistoryResponse(Booking booking,
            Map<Integer, List<Ticket>> ticketsByBookingId, Map<Integer, TripDTO> tripMap) {
        try {
            TripDTO trip = tripMap.get(booking.getTripId());
            List<Ticket> tickets = ticketsByBookingId.getOrDefault(booking.getId(), List.of());
            List<TicketResponse> ticketResponses = tickets.stream()
                    .map(TicketMapper::toResponse)
                    .collect(Collectors.toList());
            return BookingHistoryResponse.builder()
                    .id(booking.getId())
                    .status(booking.getStatus())
                    .totalPrice(booking.getTotalPrice())
                    .numberOfTickets(booking.getNumberOfTickets())
                    .userId(booking.getUserId())
                    .tripId(booking.getTripId())
                    .seatCodes(booking.getSeatCodes())
                    .orderCode(booking.getOrderCode())
                    .createdAt(booking.getCreatedAt())
                    .updatedAt(booking.getUpdatedAt())
                    .trip(trip)
                    .tickets(ticketResponses)
                    .build();
        } catch (Exception e) {
            throw new BookingHistoryException("Failed to map booking history response", e);
        }
    }
}
