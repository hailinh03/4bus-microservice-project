package com.mss.project.booking_service.payload.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsRequest {
    @NotBlank(message = "Period is required")
    @Pattern(regexp = "^(LAST_7_DAYS|LAST_30_DAYS|LAST_3_MONTHS|LAST_6_MONTHS|LAST_YEAR|CUSTOM)$", message = "Period must be one of: LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_6_MONTHS, LAST_YEAR, CUSTOM")
    @Builder.Default
    private String period = "LAST_30_DAYS";

    // For CUSTOM period
    private String startDate; // yyyy-MM-dd format
    private String endDate; // yyyy-MM-dd format

    // Optional filters
    @Builder.Default
    private Boolean includeRefunds = true;
    @Builder.Default
    private Boolean includeProjections = false;
    @Builder.Default
    private String timezone = "UTC";
}
