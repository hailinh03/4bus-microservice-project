package com.mss.project.booking_service.payload.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRefundRequest {
    @NotNull(message = "Refund payment ID is required")
    private Long refundPaymentId;

    @NotBlank(message = "Proof image URL is required")
    private String proofImageUrl;

    @NotBlank(message = "Proof image public ID is required")
    private String proofImagePublicId;
}
