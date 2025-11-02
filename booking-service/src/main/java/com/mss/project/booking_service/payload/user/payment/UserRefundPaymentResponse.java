package com.mss.project.booking_service.payload.user.payment;

import com.mss.project.booking_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRefundPaymentResponse {
    // Refund payment information
    private Long refundPaymentId;
    private PaymentStatus status;
    private Integer refundAmount;
    private String refundReason;
    private Instant refundRequestedAt;
    private Instant refundProcessedAt;
    private String proofImageUrl; // Users can see proof images if processed
    private Instant createdAt;
    private Instant updatedAt;

    // Original payment information
    private OriginalPaymentInfo originalPayment;

    // Booking information
    private BookingInfo booking;

    // Trip information
    private TripInfo trip;

    // Ticket information
    private List<TicketInfo> tickets;

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
        private Instant originalCreatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingInfo {
        private Integer bookingId;
        private String bookingCode;
        private Integer numberOfTickets;
        private Integer totalPrice;
        private String seatCodes;
        private Instant bookingCreatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripInfo {
        private Long tripId;
        private String tripCode;
        private String departureLocation;
        private String arrivalLocation;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String busType;
        private Integer basePrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfo {
        private Integer ticketId;
        private String seatCode;
        private String seatId;
        private Integer price;
        private String status;
        private String adminNote; // Only if relevant to user (e.g., cancellation reason)
        private Instant ticketCreatedAt;
    }
}
