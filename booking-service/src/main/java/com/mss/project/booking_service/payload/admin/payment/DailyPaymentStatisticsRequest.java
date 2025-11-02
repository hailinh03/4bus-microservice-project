package com.mss.project.booking_service.payload.admin.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPaymentStatisticsRequest {
    @Builder.Default
    private String period = "LAST_30_DAYS"; // LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS
}
