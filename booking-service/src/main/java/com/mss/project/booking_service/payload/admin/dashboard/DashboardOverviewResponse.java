package com.mss.project.booking_service.payload.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private RevenueOverview revenueOverview;
    private PaymentSummary paymentSummary;
    private BookingSummary bookingSummary;
    private TicketSummary ticketSummary;
    private RefundSummary refundSummary;
    private LocalDate generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueOverview {
        private Long totalRevenue;
        private Long monthlyRevenue;
        private Long dailyRevenue;
        private Double monthlyGrowthPercentage;
        private String growthDirection; // "UP", "DOWN", "STABLE"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private Long totalPayments;
        private Long completedPayments;
        private Long pendingPayments;
        private Long failedPayments;
        private Long processingPayments;
        private Double successRate; // percentage of completed payments
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingSummary {
        private Long totalBookings;
        private Long confirmedBookings;
        private Long pendingBookings;
        private Long cancelledBookings;
        private Long completedBookings;
        private Double completionRate; // percentage of completed bookings
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketSummary {
        private Long totalTickets;
        private Long activeTickets;
        private Long usedTickets;
        private Long cancelledTickets;
        private Long expiredTickets;
        private Double utilizationRate; // percentage of used tickets
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundSummary {
        private Long totalRefunds;
        private Long processingRefunds;
        private Long completedRefunds;
        private Long rejectedRefunds;
        private Long totalRefundAmount;
        private Double refundRate; // percentage of payments that were refunded
    }
}
