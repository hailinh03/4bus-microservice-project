package com.mss.project.booking_service.controller;

import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.exception.BookingHistoryException;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.PagedResponse;
import com.mss.project.booking_service.payload.booking.BookingHistoryListRequest;
import com.mss.project.booking_service.payload.booking.BookingHistoryResponse;
import com.mss.project.booking_service.payload.booking.BookingRequest;
import com.mss.project.booking_service.payload.booking.BookingResponse;
import com.mss.project.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/booking")
@Tag(name = "Booking Management", description = "APIs for booking management")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookTrip(@RequestBody BookingRequest bookingRequest) {
        BookingResponse response = bookingService.initiateBooking(bookingRequest);
        ApiResponse<BookingResponse> apiResponse = ApiResponse.<BookingResponse>builder()
                .message("Booking initiated successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get my booking history", description = "Retrieve authenticated user's booking history with trip and ticket details")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponse<BookingHistoryResponse>>> getMyBookingHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Filter by booking status") @RequestParam(required = false) BookingStatus status,
            @Parameter(description = "Filter by trip ID") @RequestParam(required = false) Integer tripId,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Input validation
        if (page < 0) {
            return ResponseEntity.badRequest().body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message("Page number must be non-negative")
                    .errors("INVALID_PAGE_NUMBER")
                    .build());
        }

        if (size <= 0 || size > 50) {
            return ResponseEntity.badRequest().body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message("Page size must be between 1 and 50")
                    .errors("INVALID_PAGE_SIZE")
                    .build());
        }

        if (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC")) {
            return ResponseEntity.badRequest().body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message("Sort direction must be ASC or DESC")
                    .errors("INVALID_SORT_DIRECTION")
                    .build());
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message("Start date must be before end date")
                    .errors("INVALID_DATE_RANGE")
                    .build());
        }

        try {
            BookingHistoryListRequest request = BookingHistoryListRequest.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .status(status)
                    .tripId(tripId)
                    .startDate(startDate != null ? startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null)
                    .endDate(endDate != null ? endDate.atTime(23,59,59).toInstant(java.time.ZoneOffset.UTC) : null)
                    .build();

            Page<BookingHistoryResponse> bookingHistory = bookingService.getMyBookingHistory(request);
            PagedResponse<BookingHistoryResponse> pagedResponse = new PagedResponse<>(
                bookingHistory.getContent(),
                bookingHistory.getNumber(),
                bookingHistory.getSize(),
                bookingHistory.getTotalElements(),
                bookingHistory.getTotalPages(),
                bookingHistory.isLast()
            );
            return ResponseEntity.ok(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(true)
                    .message("Booking history retrieved successfully")
                    .data(pagedResponse)
                    .build());

        } catch (BookingHistoryException e) {
            return ResponseEntity.status(400).body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("BOOKING_HISTORY_ERROR")
                    .build());
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.status(401).body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message("Authentication required")
                    .errors("AUTHENTICATION_REQUIRED")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<PagedResponse<BookingHistoryResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve booking history")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }
}
