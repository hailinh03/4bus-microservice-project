package com.mss.project.booking_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TicketCancelledEvent extends ApplicationEvent {
    private final Long ticketId;
    private final Long paymentId;
    private final Integer refundAmount;
    private final String refundReason;
    private final Long userId;

    public TicketCancelledEvent(Object source, Long ticketId, Long paymentId, Integer refundAmount,
            String refundReason, Long userId) {
        super(source);
        this.ticketId = ticketId;
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.userId = userId;
    }
}
