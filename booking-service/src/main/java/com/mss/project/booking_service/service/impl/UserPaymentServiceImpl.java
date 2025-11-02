package com.mss.project.booking_service.service.impl;

import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.entities.Payment;
import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.exception.PaymentLinkException;
import com.mss.project.booking_service.payload.PagedResponse;
import com.mss.project.booking_service.payload.trip.TripDTO;
import com.mss.project.booking_service.payload.user.payment.UserRefundPaymentFilter;
import com.mss.project.booking_service.payload.user.payment.UserRefundPaymentResponse;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.repository.PaymentRepository;
import com.mss.project.booking_service.repository.TicketRepository;
import com.mss.project.booking_service.service.TripService;
import com.mss.project.booking_service.service.UserPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPaymentServiceImpl implements UserPaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TripService tripService;

    @Override
    @Transactional(readOnly = true)
    public List<UserRefundPaymentResponse> getUserRefundPayments(Long userId) {
        try {
            log.info("Fetching refund payments for user: {}", userId);

            // Find all refund payments for bookings belonging to this user
            List<Payment> refundPayments = paymentRepository.findRefundPaymentsByUserId(userId);

            // Map to response DTOs
            return refundPayments.stream()
                    .map(this::mapToUserRefundResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching refund payments for user {}: {}", userId, e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch refund payments: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserRefundPaymentResponse> getUserRefundPayments(Long userId, UserRefundPaymentFilter filter) {
        try {
            log.info("Fetching paginated refund payments for user: {} with filter: {}", userId, filter);

            // Create pageable object
            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

            // Convert booking code to Long if provided
            Long bookingCodeAsLong = null;
            if (filter.getBookingCode() != null && !filter.getBookingCode().trim().isEmpty()) {
                try {
                    bookingCodeAsLong = Long.parseLong(filter.getBookingCode().trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid booking code format: {}", filter.getBookingCode());
                    // Set to impossible value so no results match
                    bookingCodeAsLong = -1L;
                }
            }

            // Find refund payments with filter
            Page<Payment> refundPage = paymentRepository.findRefundPaymentsByUserIdWithFilter(
                    userId,
                    filter.getStatus(),
                    filter.getCreatedFromDate(),
                    filter.getCreatedToDate(),
                    filter.getProcessedFromDate(),
                    filter.getProcessedToDate(),
                    filter.getMinAmount(),
                    filter.getMaxAmount(),
                    filter.getBookingCode(),
                    bookingCodeAsLong,
                    filter.getSearchKeyword(),
                    filter.getSortBy(),
                    filter.getSortDirection(),
                    pageable);

            // Map to response DTOs
            List<UserRefundPaymentResponse> content = refundPage.getContent().stream()
                    .map(this::mapToUserRefundResponse)
                    .collect(Collectors.toList());

            // Create paginated response
            return new PagedResponse<>(
                    content,
                    refundPage.getNumber(),
                    refundPage.getSize(),
                    refundPage.getTotalElements(),
                    refundPage.getTotalPages(),
                    refundPage.isLast());

        } catch (Exception e) {
            log.error("Error fetching paginated refund payments for user {}: {}", userId, e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch refund payments: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserRefundPaymentResponse getUserRefundPaymentById(Long userId, Long refundPaymentId) {
        try {
            log.info("Fetching refund payment {} for user: {}", refundPaymentId, userId);

            // Find the refund payment
            Payment refundPayment = paymentRepository.findById(refundPaymentId)
                    .orElseThrow(
                            () -> new PaymentLinkException("Refund payment not found with id: " + refundPaymentId));

            // Validate that this is a refund payment
            if (!refundPayment.getIsRefund()) {
                throw new PaymentLinkException("Payment is not a refund payment");
            }

            // Validate that this refund belongs to the user
            if (!refundPayment.getBooking().getUserId().equals(userId)) {
                throw new PaymentLinkException("Access denied. This refund payment does not belong to you");
            }

            return mapToUserRefundResponse(refundPayment);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching refund payment {} for user {}: {}", refundPaymentId, userId, e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch refund payment: " + e.getMessage());
        }
    }

    private UserRefundPaymentResponse mapToUserRefundResponse(Payment refundPayment) {
        try {
            Booking booking = refundPayment.getBooking();

            // Get original payment
            Payment originalPayment = paymentRepository.findById(refundPayment.getOriginalPaymentId())
                    .orElseThrow(() -> new PaymentLinkException("Original payment not found"));

            // Get trip information
            TripDTO tripDTO = null;
            try {
                var tripResponse = tripService.getTripById(booking.getTripId());
                if (tripResponse.isSuccess()) {
                    tripDTO = tripResponse.getData();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch trip information for trip {}: {}", booking.getTripId(), e.getMessage());
            }

            // Get tickets for this booking
            List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());

            // Build response
            return UserRefundPaymentResponse.builder()
                    .refundPaymentId(refundPayment.getId())
                    .status(refundPayment.getStatus())
                    .refundAmount(refundPayment.getRefundAmount())
                    .refundReason(refundPayment.getRefundReason())
                    .refundRequestedAt(refundPayment.getRefundRequestedAt())
                    .refundProcessedAt(refundPayment.getRefundProcessedAt())
                    .proofImageUrl(refundPayment.getProofImageUrl()) // Users can see proof if available
                    .createdAt(refundPayment.getCreatedAt())
                    .updatedAt(refundPayment.getUpdatedAt())
                    .originalPayment(mapToOriginalPaymentInfo(originalPayment))
                    .booking(mapToBookingInfo(booking))
                    .trip(mapToTripInfo(tripDTO, booking))
                    .tickets(mapToTicketInfoList(tickets))
                    .build();

        } catch (Exception e) {
            log.error("Error mapping refund payment to user response: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to process refund payment data: " + e.getMessage());
        }
    }

    private UserRefundPaymentResponse.OriginalPaymentInfo mapToOriginalPaymentInfo(Payment originalPayment) {
        return UserRefundPaymentResponse.OriginalPaymentInfo.builder()
                .originalPaymentId(originalPayment.getId())
                .originalStatus(originalPayment.getStatus())
                .originalAmount(originalPayment.getAmount())
                .description(originalPayment.getDescription())
                .paymentDate(originalPayment.getPaymentDate())
                .originalCreatedAt(originalPayment.getCreatedAt())
                .build();
    }

    private UserRefundPaymentResponse.BookingInfo mapToBookingInfo(Booking booking) {
        return UserRefundPaymentResponse.BookingInfo.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getOrderCode() != null ? booking.getOrderCode().toString() : null)
                .numberOfTickets(booking.getNumberOfTickets())
                .totalPrice(booking.getTotalPrice().intValue())
                .seatCodes(booking.getSeatCodes())
                .bookingCreatedAt(booking.getCreatedAt())
                .build();
    }

    private UserRefundPaymentResponse.TripInfo mapToTripInfo(TripDTO tripDTO, Booking booking) {
        if (tripDTO == null) {
            // Return minimal info if trip service is unavailable
            return UserRefundPaymentResponse.TripInfo.builder()
                    .tripId(booking.getTripId().longValue())
                    .tripCode("N/A")
                    .departureLocation("N/A")
                    .arrivalLocation("N/A")
                    .build();
        }

        return UserRefundPaymentResponse.TripInfo.builder()
                .tripId((long) tripDTO.getId())
                .tripCode(tripDTO.getName())
                .departureLocation(tripDTO.getFrom() != null && tripDTO.getFrom().getRoutePoint() != null
                        ? tripDTO.getFrom().getRoutePoint().getName()
                        : tripDTO.getOrigin())
                .arrivalLocation(tripDTO.getTo() != null && tripDTO.getTo().getRoutePoint() != null
                        ? tripDTO.getTo().getRoutePoint().getName()
                        : tripDTO.getDestination())
                .departureTime(tripDTO.getStartTime())
                .arrivalTime(tripDTO.getStartTime() != null && tripDTO.getEstimateDuration() > 0
                        ? tripDTO.getStartTime().plusMinutes(tripDTO.getEstimateDuration())
                        : null)
                .busType(tripDTO.getBus() != null && tripDTO.getBus().getCategory() != null
                        ? tripDTO.getBus().getCategory().getName()
                        : "N/A")
                .basePrice(null) // Bus base price not available in current DTO
                .build();
    }

    private List<UserRefundPaymentResponse.TicketInfo> mapToTicketInfoList(List<Ticket> tickets) {
        return tickets.stream()
                .map(this::mapToTicketInfo)
                .collect(Collectors.toList());
    }

    private UserRefundPaymentResponse.TicketInfo mapToTicketInfo(Ticket ticket) {
        return UserRefundPaymentResponse.TicketInfo.builder()
                .ticketId(ticket.getId())
                .seatCode(ticket.getSeatCode())
                .seatId(ticket.getSeatId())
                .price(ticket.getPrice())
                .status(ticket.getStatus().toString())
                .adminNote(ticket.getAdminNote()) // Include admin note if relevant to user
                .ticketCreatedAt(ticket.getCreatedAt())
                .build();
    }
}
