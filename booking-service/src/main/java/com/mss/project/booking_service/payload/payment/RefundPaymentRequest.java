package com.mss.project.booking_service.payload.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentRequest {
    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be positive")
    private int refundAmount;

    @NotNull(message = "Refund reason is required")
    @Size(min = 5, max = 500, message = "Refund reason must be between 5 and 500 characters")
    private String refundReason;
}
