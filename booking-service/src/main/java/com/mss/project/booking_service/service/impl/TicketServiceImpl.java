package com.mss.project.booking_service.service.impl;

import com.mss.project.booking_service.payload.notification.NotificationRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.ticket.CancelTicketRequest;
import com.mss.project.booking_service.payload.ticket.CancelTicketResponse;
import com.mss.project.booking_service.payload.ticket.TicketRequest;
import com.mss.project.booking_service.payload.ticket.TicketResponse;
import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.enums.TicketStatus;
import com.mss.project.booking_service.exception.BookingException;
import com.mss.project.booking_service.mapper.TicketMapper;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.repository.TicketRepository;
import com.mss.project.booking_service.service.TicketService;
import com.mss.project.booking_service.service.TripService;
import com.mss.project.booking_service.service.UserService;
import com.mss.project.booking_service.event.TicketCancelledEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketServiceImpl implements TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TripService tripService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PaymentServiceImpl paymentServiceImpl;
    private NotificationRequest noti;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketRequest ticketRequest) {
        Ticket ticket = TicketMapper.toEntity(ticketRequest);
        if (ticketRequest.getBookingId() != null) {
            Booking booking = bookingRepository.findById(ticketRequest.getBookingId()).orElse(null);
            ticket.setBooking(booking);
        }
        Ticket saved = ticketRepository.save(ticket);
        return TicketMapper.toResponse(saved);
    }

    @Override
    public TicketResponse getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .map(TicketMapper::toResponse)
                .orElse(null);
    }

    @Override
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(TicketMapper::toResponse)
                .toList();
    }

    @Override
    public TicketResponse updateTicket(Integer id, TicketRequest ticketRequest) {
        if (!ticketRepository.existsById(id)) {
            return null;
        }
        Ticket ticket = TicketMapper.toEntity(ticketRequest);
        ticket.setId(id);
        if (ticketRequest.getBookingId() != null) {
            Booking booking = bookingRepository.findById(ticketRequest.getBookingId()).orElse(null);
            ticket.setBooking(booking);
        }
        Ticket updated = ticketRepository.save(ticket);
        return TicketMapper.toResponse(updated);
    }

    @Override
    public void deleteTicket(Integer id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public List<String> getBookedSeatsByTripId(Integer tripId) {
        return ticketRepository.findSeatIdByTripIdAndStatus(tripId, TicketStatus.ACTIVE);
    }

    @Override
    @Transactional
    public CancelTicketResponse cancelTicket(Integer ticketId, CancelTicketRequest request) {
        try {
            // Extract user ID from security context
            Long userId = extractUserIdFromSecurityContext();

            // Find ticket and validate ownership
            Ticket ticket = ticketRepository.findByIdAndUserId(ticketId, userId)
                    .orElseThrow(() -> new BookingException(
                            "Ticket not found or you don't have permission to cancel this ticket"));

            // Validate ticket status - only ACTIVE tickets can be cancelled
            if (ticket.getStatus() != TicketStatus.ACTIVE) {
                throw new BookingException(
                        "Cannot cancel ticket. Only active tickets can be cancelled. Current status: "
                                + ticket.getStatus());
            }

            // Update ticket status to CANCELLED
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setAdminNote(
                    request.getCancellationReason() != null ? request.getCancellationReason()
                            : "Người dùng yêu cầu hủy vé");
            eventPublisher.publishEvent(new TicketCancelledEvent(
                    this,
                    ticket.getId().longValue(),
                    ticket.getBooking().getOrderCode(),
                    ticket.getPrice(),
                    "Hủy vé bởi người dùng: " + ticket.getAdminNote(),
                    ticket.getBooking().getUserId()));
            Ticket cancelledTicket = ticketRepository.save(ticket);

            log.info("User {} cancelled ticket {} with reason: {}", userId, ticketId, request.getCancellationReason());

            return mapToCancelTicketResponse(cancelledTicket);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error cancelling ticket {}: {}", ticketId, e.getMessage(), e);
            throw new BookingException("Failed to cancel ticket: " + e.getMessage());
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

    private CancelTicketResponse mapToCancelTicketResponse(Ticket ticket) {
        return CancelTicketResponse.builder()
                .id(ticket.getId())
                .status(ticket.getStatus())
                .price(ticket.getPrice())
                .seatCode(ticket.getSeatCode())
                .seatId(ticket.getSeatId())
                .bookingId(ticket.getBooking().getId())
                .tripId(ticket.getTripId())
                .cancellationReason(ticket.getAdminNote())
                .cancelledAt(ticket.getUpdatedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void markTicketsUsedByTripId(Integer tripId) {
        try {
            List<Ticket> tickets = ticketRepository.findByTripIdAndStatus(tripId, TicketStatus.ACTIVE);
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.USED);
            }
            ticketRepository.saveAll(tickets);
        } catch (Exception e) {
            log.error("Error marking tickets as USED for tripId {}: {}", tripId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void markTicketsExpiredByTripId(Integer tripId) {
        try {
            // get all active tickets by tripId
            List<Ticket> tickets = ticketRepository.findByTripIdAndStatus(tripId, TicketStatus.ACTIVE);

            // handle case when no tickets found
            if (tickets.isEmpty()) {
                log.warn("No active tickets found for tripId {}", tripId);
                return;
            }

            // get all userIds who booked tickets in this trip
            List<Long> userIds = tickets.stream()
                    .map(Ticket::getBooking)
                    .map(Booking::getUserId)
                    .distinct()
                    .toList();

            // get trip name for notification
            String tripName = tripService.getTripById(tripId).getData().getName();
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.EXPIRED);
            }

            // calculate refund amount sum for all tickets of an user
            for (Long userId : userIds) {
                // send notification to user
                try {
                    NotificationRequest noti = new NotificationRequest();
                    noti.setTitle("Chuyến đi đã bị hủy");
                    noti.setContent("Các vé trong chuyến đi \"" + tripName
                            + "\" đã bị hủy bởi quản trị viên. Yêu cầu hoàn tiền đã được gửi đi. Nếu bạn có thắc mắc gì, vui lòng liên hệ chăm sóc khách hàng để biết thêm chi tiết.");
                    noti.setUrl("/refund-history");
                    noti.setUserId(userId);
                    userService.sendNotification(noti);
                } catch (Exception e) {
                    log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
                }
                // get all booking ids of the user in this trip
                List<Integer> bookingIds = tickets.stream()
                        .filter(ticket -> ticket.getBooking().getUserId().equals(userId))
                        .map(ticket -> ticket.getBooking().getId())
                        .distinct()
                        .toList();

                // get original payment id of the user in this trip, but only the one with the
                // same booking id
                for (Integer bookingId : bookingIds) {
                    Long paymentId = tickets.stream()
                            .filter(ticket -> ticket.getBooking().getId().equals(bookingId))
                            .findFirst()
                            .map(ticket -> ticket.getBooking().getOrderCode())
                            .orElse(null);
                    // get all tickets of the user in this trip
                    int totalRefundAmount = tickets.stream()
                            .filter(ticket -> ticket.getBooking().getId().equals(bookingId))
                            .mapToInt(Ticket::getPrice)
                            .sum();
                    if (totalRefundAmount <= 0) {
                        log.warn("No refund amount for user {} in trip {}", userId, tripId);
                        continue;
                    }
                    // set refund amount for the request
                    RefundPaymentRequest refundRequest = new RefundPaymentRequest();
                    refundRequest.setRefundAmount(totalRefundAmount);
                    refundRequest.setRefundReason("Chuyến đi \"" + tripName + "\" đã bị hủy bởi quản trị viên");
                    refundRequest.setPaymentId(paymentId);
                    paymentServiceImpl.createRefundPayment(refundRequest);
                }

            }
            // publish ticket cancelled event
            ticketRepository.saveAll(tickets);
            log.info("Successfully marked {} tickets as EXPIRED for tripId {}", tickets.size(), tripId);
        } catch (Exception e) {
            log.error("Error marking tickets as EXPIRED for tripId {}: {}", tripId, e.getMessage());
            throw new BookingException("Failed to mark tickets as EXPIRED for trip ID: " + tripId);
        }
    }

    static Long generateOrderCode() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 900);
        String orderCodeStr = String.format("%d%03d", timestamp, random);
        return Long.parseLong(orderCodeStr);
    }

}
