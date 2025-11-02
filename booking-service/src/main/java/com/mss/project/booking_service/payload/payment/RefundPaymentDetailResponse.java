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
public class RefundPaymentDetailResponse {
    // Refund payment information
    private Long refundPaymentId;
    private PaymentStatus status;
    private Integer refundAmount;
    private String refundReason;
    private Instant refundRequestedAt;
    private Instant refundProcessedAt;
    private String proofImageUrl;
    private String proofImagePublicId;
    private String adminNote;
    private Instant createdAt;
    private Instant updatedAt;

    // Original payment information
    private OriginalPaymentInfo originalPayment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OriginalPaymentInfo {
        private Long originalPaymentId;
        private PaymentStatus originalStatus;
        private Integer originalAmount;
        private String description;
        private LocalDateTime paymentDate;
        private Long bookingId;
        private Instant originalCreatedAt;
        private Instant originalUpdatedAt;
    }
}
