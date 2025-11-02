package com.mss.project.booking_service.payload.admin.dashboard;

import com.mss.project.booking_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAnalyticsResponse {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private PaymentTrends trends;
    private PaymentStatusBreakdown statusBreakdown;
    private List<DailyPaymentTrend> dailyTrends;
    private RevenueMetrics revenueMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentTrends {
        private Long totalPayments;
        private Long totalAmount;
        private Double averagePaymentAmount;
        private Double growthRate; // compared to previous period
        private String trendDirection; // "UP", "DOWN", "STABLE"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStatusBreakdown {
        private Long completedPayments;
        private Long pendingPayments;
        private Long failedPayments;
        private Long cancelledPayments;
        private Long processingPayments;
        private Long resolvedPayments;
        private Map<PaymentStatus, Double> statusPercentages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPaymentTrend {
        private LocalDate date;
        private Long paymentCount;
        private Long totalAmount;
        private Long completedCount;
        private Long failedCount;
        private Double successRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private Long totalRevenue; // completed payments only
        private Long projectedRevenue; // including pending/processing
        private Long lostRevenue; // failed/cancelled payments
        private Double revenueEfficiency; // total revenue / projected revenue
    }
}
