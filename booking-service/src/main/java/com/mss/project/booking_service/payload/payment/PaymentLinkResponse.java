package com.mss.project.booking_service.payload.payment;

import lombok.Data;

import java.util.Map;

@Data
public class PaymentLinkResponse {
    private String checkoutUrl;
    private String qrCode;
    private String status;
    private String message;
    private Map<String, Object> data;
}
