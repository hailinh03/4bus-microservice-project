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
public class RefundAnalyticsResponse {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private RefundSummary summary;
    private List<DailyRefundData> dailyData;
    private RefundTrends trends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundSummary {
        private Long totalRefundRequests;
        private Long processingRefunds;
        private Long completedRefunds;
        private Long rejectedRefunds;
        private Long totalRefundAmount;
        private Long completedRefundAmount;
        private Double averageRefundAmount;
        private Double refundRate; // percentage of total payments that were refunded
        private Double approvalRate; // percentage of refund requests that were approved
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRefundData {
        private LocalDate date;
        private Long refundRequests;
        private Long processedRefunds;
        private Long completedRefunds;
        private Long rejectedRefunds;
        private Long refundAmount;
        private Double approvalRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundTrends {
        private Double requestGrowthRate; // compared to previous period
        private Double amountGrowthRate;
        private String trendDirection; // "UP", "DOWN", "STABLE"
        private String primaryRefundReason; // most common refund reason
        private Double processingTimeAverage; // average days to process refund
    }
}
