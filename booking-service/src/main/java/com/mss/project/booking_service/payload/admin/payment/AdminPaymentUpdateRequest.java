package com.mss.project.booking_service.payload.admin.payment;

import com.mss.project.booking_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentUpdateRequest {
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;
    
    private String description;
    private String adminNote;
}
