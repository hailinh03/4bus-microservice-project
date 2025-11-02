package com.mss.project.booking_service.payload.admin.payment;

import com.mss.project.booking_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentListRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    private PaymentStatus status;
    private Long bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minAmount;
    private Integer maxAmount;
}
