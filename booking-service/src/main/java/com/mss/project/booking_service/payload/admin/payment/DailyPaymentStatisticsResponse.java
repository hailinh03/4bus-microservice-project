package com.mss.project.booking_service.payload.admin.payment;

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
public class DailyPaymentStatisticsResponse {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalPayments;
    private Long totalAmount;
    private List<DailyPaymentData> dailyData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPaymentData {
        private LocalDate date;
        private Long paymentCount;
        private Long totalAmount;
        private Long completedPayments;
        private Long pendingPayments;
        private Long failedPayments;
        private Long cancelledPayments;
        private Long processingPayments;
        private Long resolvedPayments;
    }
}
