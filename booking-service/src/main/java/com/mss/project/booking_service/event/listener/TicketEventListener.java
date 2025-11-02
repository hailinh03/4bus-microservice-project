package com.mss.project.booking_service.event.listener;

import com.mss.project.booking_service.event.TicketCancelledEvent;
import com.mss.project.booking_service.payload.notification.NotificationRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.service.PaymentService;
import com.mss.project.booking_service.service.TicketService;
import com.mss.project.booking_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {

    private final PaymentService paymentService;
    private final UserService userService;
    private final TicketService ticketService;

    @EventListener
    @Transactional
    public void handleTicketCancelled(TicketCancelledEvent event) {
        try {
            log.info("Processing ticket cancelled event for ticket ID: {}", event.getTicketId());

            RefundPaymentRequest refundPaymentRequest = new RefundPaymentRequest();
            refundPaymentRequest.setRefundAmount(event.getRefundAmount());
            refundPaymentRequest.setRefundReason(event.getRefundReason());
            refundPaymentRequest.setPaymentId(event.getPaymentId());

            paymentService.createRefundPayment(refundPaymentRequest);
            try {

                NotificationRequest noti = new NotificationRequest();
                noti.setTitle("Vé đã bị hủy");
                noti.setContent("Yêu cầu hủy vé của bạn đã được xử lý. Số tiền sẽ được hoàn lại trong thời gian sớm nhất.");
                noti.setUrl("/booking-history");
                noti.setUserId(event.getUserId());
                userService.sendNotification(noti);
            } catch (Exception e) {
                log.error("Failed to send notification to user {}: {}", event.getUserId(), e.getMessage());
            }
            log.info("Created refund payment for cancelled ticket ID: {}", event.getTicketId());
        } catch (Exception e) {
            log.error("Error handling ticket cancelled event for ticket ID: {}", event.getTicketId(), e);
            // Don't rethrow to prevent ticket cancellation from failing
        }
    }
}
