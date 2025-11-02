package com.mss.project.booking_service.payload.payment;

import com.mss.project.booking_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentResponse {
    private Long refundPaymentId;
    private Long originalPaymentId;
    private PaymentStatus status;
    private Integer refundAmount;
    private String refundReason;
    private LocalDateTime paymentDate;
    private Instant refundRequestedAt;
    private Instant refundProcessedAt;
    private Long bookingId;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String adminNote;
    private String proofImageUrl;
    private String proofImagePublicId;
}
