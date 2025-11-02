package com.mss.project.booking_service.payload.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private RevenueOverview overview;
    private List<DailyRevenueData> dailyRevenue;
    private List<MonthlyRevenueData> monthlyRevenue;
    private RevenueProjection projection;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueOverview {
        private Long totalRevenue;
        private Long netRevenue; // after refunds
        private Long grossRevenue; // before refunds
        private Long refundedAmount;
        private Double refundRate;
        private Double averageTransactionValue;
        private Integer totalTransactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenueData {
        private LocalDate date;
        private Long revenue;
        private Long netRevenue;
        private Integer transactionCount;
        private Long refundAmount;
        private Double averageTransactionValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueData {
        private String month; // "2025-01"
        private Long revenue;
        private Long netRevenue;
        private Integer transactionCount;
        private Double growthRate; // compared to previous month
        private String growthDirection; // "UP", "DOWN", "STABLE"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueProjection {
        private Long projectedMonthlyRevenue;
        private Long projectedAnnualRevenue;
        private Double confidenceLevel; // prediction confidence
        private String projectionBasis; // "CURRENT_TREND", "HISTORICAL_AVERAGE", etc.
    }
}
