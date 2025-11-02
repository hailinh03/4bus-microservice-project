package com.mss.project.booking_service.payload.user.payment;

import com.mss.project.booking_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRefundPaymentFilter {

    // Status filter
    private PaymentStatus status;

    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdFromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdToDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate processedFromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate processedToDate;

    // Amount range filters
    private Integer minAmount;
    private Integer maxAmount;

    // Trip information filters
    private String departureLocation;
    private String arrivalLocation;
    private String tripCode;

    // Booking information filters
    private String bookingCode;

    // General search
    private String searchKeyword; // Search in refund reason, booking code, trip code

    // Sorting options
    private String sortBy; // createdAt, processedAt, amount, status
    private String sortDirection; // ASC, DESC (default DESC)

    // Pagination
    private Integer page; // 0-based page number, default 0
    private Integer size; // page size, default 10, max 100

    public Integer getPage() {
        return page != null && page >= 0 ? page : 0;
    }

    public Integer getSize() {
        if (size == null)
            return 10;
        return Math.min(Math.max(size, 1), 100); // Min 1, Max 100
    }

    public String getSortDirection() {
        return sortDirection != null && sortDirection.equalsIgnoreCase("ASC") ? "ASC" : "DESC";
    }

    public String getSortBy() {
        if (sortBy == null)
            return "createdAt";
        // Validate sort field
        return switch (sortBy.toLowerCase()) {
            case "createdat", "created_at" -> "createdAt";
            case "processedat", "processed_at" -> "processedAt";
            case "amount" -> "amount";
            case "status" -> "status";
            default -> "createdAt";
        };
    }
}
