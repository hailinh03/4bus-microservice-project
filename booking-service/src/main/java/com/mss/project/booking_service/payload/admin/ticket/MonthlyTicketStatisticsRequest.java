package com.mss.project.booking_service.payload.admin.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTicketStatisticsRequest {
    @Min(value = 2020, message = "Year must be 2020 or later")
    @Max(value = 2030, message = "Year must be 2030 or earlier")
    @Builder.Default
    private Integer year = java.time.Year.now().getValue();
}
