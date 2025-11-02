package com.mss.project.booking_service.payload.admin.payment;

import com.mss.project.booking_service.enums.PaymentStatus;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingResponse;
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
public class AdminPaymentResponse {
    private Long id;
    private PaymentStatus status;
    private Integer amount;
    private String description;
    private LocalDateTime paymentDate;
    private Long bookingId;
    private Instant createdAt;
    private Instant updatedAt;

    // Refund-related fields
    private Integer refundAmount;
    private String refundReason;
    private Instant refundRequestedAt;
    private Instant refundProcessedAt;
    private Long originalPaymentId;
    private Boolean isRefund;

    // Admin fields
    private String adminNote;

    // Additional admin fields
    private AdminBookingResponse booking;
}
