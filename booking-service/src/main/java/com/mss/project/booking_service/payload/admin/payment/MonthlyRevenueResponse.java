package com.mss.project.booking_service.payload.admin.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private YearMonth currentMonth;
    private YearMonth previousMonth;
    private Long currentMonthRevenue;
    private Long previousMonthRevenue;
    private BigDecimal percentageChange;
    private String changeDirection; // "INCREASE", "DECREASE", "NO_CHANGE"
    private String formattedPercentage; // e.g., "+15.5%", "-8.2%", "0.0%"

    // Additional metrics
    private Long currentMonthPaymentCount;
    private Long previousMonthPaymentCount;
    private Long currentMonthCompletedPayments;
    private Long previousMonthCompletedPayments;
}
