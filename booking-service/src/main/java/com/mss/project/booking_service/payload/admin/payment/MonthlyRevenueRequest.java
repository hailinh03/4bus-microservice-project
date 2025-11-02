package com.mss.project.booking_service.payload.admin.payment;

import lombok.Data;

@Data
public class MonthlyRevenueRequest {
    // No specific parameters needed as we're comparing current month vs previous
    // month
    // Could be extended in the future to support custom month comparison
}
