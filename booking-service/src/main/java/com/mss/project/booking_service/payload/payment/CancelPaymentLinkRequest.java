package com.mss.project.booking_service.payload.payment;

import lombok.Data;

@Data
public class CancelPaymentLinkRequest {
    private Long orderCode;
    private String cancellationReason;
}
