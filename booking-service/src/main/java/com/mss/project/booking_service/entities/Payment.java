package com.mss.project.booking_service.entities;

import com.mss.project.booking_service.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    @Column(nullable = false)
    Integer amount;

    String description;

    @Column(nullable = false)
    LocalDateTime paymentDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    // Refund-related fields
    @Column(name = "refund_amount")
    Integer refundAmount;

    @Column(name = "refund_reason")
    String refundReason;

    @Column(name = "refund_requested_at")
    Instant refundRequestedAt;

    @Column(name = "refund_processed_at")
    Instant refundProcessedAt;

    @Column(name = "original_payment_id")
    Long originalPaymentId;

    @Column(name = "proof_image_url")
    String proofImageUrl;

    @Column(unique = true)
    String proofImagePublicId;

    @Column(name = "is_refund")
    @Builder.Default
    Boolean isRefund = false;

    String adminNote;
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.paymentDate = now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
