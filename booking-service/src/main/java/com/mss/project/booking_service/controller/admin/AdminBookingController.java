package com.mss.project.booking_service.controller.admin;

import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingListRequest;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingResponse;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingUpdateRequest;
import com.mss.project.booking_service.service.AdminBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Booking Management", description = "Admin APIs for booking management")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @Operation(summary = "Get all bookings with filters", description = "Retrieve paginated list of all bookings with optional filters")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminBookingResponse>>> getAllBookings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Filter by booking status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by trip ID") @RequestParam(required = false) Integer tripId,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {

        try {
            AdminBookingListRequest request = AdminBookingListRequest.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .userId(userId)
                    .tripId(tripId)
                    .build();

            // Handle status enum conversion
            if (status != null && !status.isEmpty()) {
                try {
                    request.setStatus(BookingStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminBookingResponse>>builder()
                            .success(false)
                            .message("Invalid booking status: " + status)
                            .errors("INVALID_STATUS")
                            .build());
                }
            }

            // Handle date parsing
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    request.setStartDate(java.time.LocalDate.parse(startDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminBookingResponse>>builder()
                            .success(false)
                            .message("Invalid start date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            if (endDate != null && !endDate.isEmpty()) {
                try {
                    request.setEndDate(java.time.LocalDate.parse(endDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminBookingResponse>>builder()
                            .success(false)
                            .message("Invalid end date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            Page<AdminBookingResponse> bookings = adminBookingService.getAllBookings(request);

            return ResponseEntity.ok(ApiResponse.<Page<AdminBookingResponse>>builder()
                    .success(true)
                    .message("Bookings retrieved successfully")
                    .data(bookings)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Page<AdminBookingResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve bookings")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get booking by ID", description = "Retrieve booking details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable Integer id) {

        try {
            AdminBookingResponse booking = adminBookingService.getBookingById(id);

            return ResponseEntity.ok(ApiResponse.<AdminBookingResponse>builder()
                    .success(true)
                    .message("Booking retrieved successfully")
                    .data(booking)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(404).body(ApiResponse.<AdminBookingResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("BOOKING_NOT_FOUND")
                    .build());
        }
    }

    @Operation(summary = "Update booking", description = "Update booking status and details")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> updateBooking(
            @Parameter(description = "Booking ID") @PathVariable Integer id,
            @Valid @RequestBody AdminBookingUpdateRequest request) {

        try {
            AdminBookingResponse booking = adminBookingService.updateBooking(id, request);

            return ResponseEntity.ok(ApiResponse.<AdminBookingResponse>builder()
                    .success(true)
                    .message("Booking updated successfully")
                    .data(booking)
                    .build());

        } catch (Exception e) {
            log.error("Error updating booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<AdminBookingResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("UPDATE_FAILED")
                    .build());
        }
    }

    @Operation(summary = "Delete booking", description = "Delete booking from database")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @Parameter(description = "Booking ID") @PathVariable Integer id) {

        try {
            adminBookingService.deleteBooking(id);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Booking deleted successfully")
                    .build());

        } catch (Exception e) {
            log.error("Error deleting booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("DELETE_FAILED")
                    .build());
        }
    }

    @Operation(summary = "Get booking statistics", description = "Get booking count statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Long>> getBookingStatistics() {

        try {
            long count = adminBookingService.countBookingsByStatus();

            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("Statistics retrieved successfully")
                    .data(count)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving booking statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Long>builder()
                    .success(false)
                    .message("Failed to retrieve statistics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }
}
