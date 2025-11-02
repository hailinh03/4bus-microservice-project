package com.mss.project.booking_service.event.listener;

import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.event.PaymentCompletedEvent;
import com.mss.project.booking_service.payload.ticket.TicketRequest;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.service.TicketService;
import com.mss.project.booking_service.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final TicketService ticketService;
    private final BookingRepository bookingRepository;

    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("Processing payment completed event for booking ID: {}", event.getBookingId());

            // Find the booking
            Booking booking = bookingRepository.findById(event.getBookingId().intValue())
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + event.getBookingId()));

            // Create tickets for each seat
            if (booking.getSeatCodes() != null && !booking.getSeatCodes().isEmpty()) {
                String[] seatCodeArray = booking.getSeatCodes().split(",");
                for (String seatCode : seatCodeArray) {
                    if (seatCode != null && !seatCode.trim().isEmpty()) {
                        TicketRequest ticketRequest = TicketRequest.builder()
                                .status(TicketStatus.ACTIVE)
                                .price((int) Double.parseDouble(seatCode.split(":")[2])) // seatCode format:
                                                                                         // "seatId:seatCode:price"
                                .seatCode(seatCode.split(":")[1])
                                .seatId(seatCode.split(":")[0])
                                .bookingId(booking.getId())
                                .tripId(booking.getTripId())
                                .build();
                        ticketService.createTicket(ticketRequest);
                    }
                }
                log.info("Created tickets for booking ID: {}", event.getBookingId());
            }
        } catch (Exception e) {
            log.error("Error handling payment completed event for booking ID: {}", event.getBookingId(), e);
            // Don't rethrow to prevent payment processing from failing
            Booking revese = bookingRepository.findById(event.getBookingId().intValue()).orElseThrow(() -> new RuntimeException("Booking not found with id: " + event.getBookingId()));
            revese.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(revese);

        }
    }
}
