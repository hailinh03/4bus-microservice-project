package com.mss.project.booking_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {
    private final Long bookingId;
    private final Long paymentId;

    public PaymentCompletedEvent(Object source, Long bookingId, Long paymentId) {
        super(source);
        this.bookingId = bookingId;
        this.paymentId = paymentId;
    }
}
