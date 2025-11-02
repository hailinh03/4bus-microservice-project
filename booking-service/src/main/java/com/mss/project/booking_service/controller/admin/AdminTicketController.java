package com.mss.project.booking_service.controller.admin;

import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketListRequest;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketResponse;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketUpdateRequest;
import com.mss.project.booking_service.payload.admin.ticket.MonthlyTicketStatisticsRequest;
import com.mss.project.booking_service.payload.admin.ticket.MonthlyTicketStatisticsResponse;
import com.mss.project.booking_service.service.AdminTicketService;
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
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Ticket Management", description = "Admin APIs for ticket management")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    @Operation(summary = "Get all tickets with filters", description = "Retrieve paginated list of all tickets with optional filters")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminTicketResponse>>> getAllTickets(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Filter by ticket status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) Integer bookingId,
            @Parameter(description = "Filter by trip ID") @RequestParam(required = false) Integer tripId,
            @Parameter(description = "Filter by seat code") @RequestParam(required = false) String seatCode,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {

        try {
            AdminTicketListRequest request = AdminTicketListRequest.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .bookingId(bookingId)
                    .tripId(tripId)
                    .seatCode(seatCode)
                    .build();

            // Handle status enum conversion
            if (status != null && !status.isEmpty()) {
                try {
                    request.setStatus(com.mss.project.booking_service.enums.TicketStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminTicketResponse>>builder()
                            .success(false)
                            .message("Invalid ticket status: " + status)
                            .errors("INVALID_STATUS")
                            .build());
                }
            }

            // Handle date parsing
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    request.setStartDate(java.time.LocalDate.parse(startDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminTicketResponse>>builder()
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
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminTicketResponse>>builder()
                            .success(false)
                            .message("Invalid end date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            Page<AdminTicketResponse> tickets = adminTicketService.getAllTickets(request);

            return ResponseEntity.ok(ApiResponse.<Page<AdminTicketResponse>>builder()
                    .success(true)
                    .message("Tickets retrieved successfully")
                    .data(tickets)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving tickets: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Page<AdminTicketResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve tickets")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get ticket by ID", description = "Retrieve ticket details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminTicketResponse>> getTicketById(
            @Parameter(description = "Ticket ID") @PathVariable Integer id) {

        try {
            AdminTicketResponse ticket = adminTicketService.getTicketById(id);

            return ResponseEntity.ok(ApiResponse.<AdminTicketResponse>builder()
                    .success(true)
                    .message("Ticket retrieved successfully")
                    .data(ticket)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving ticket {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(404).body(ApiResponse.<AdminTicketResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("TICKET_NOT_FOUND")
                    .build());
        }
    }

    @Operation(summary = "Update ticket", description = "Update ticket status and details")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminTicketResponse>> updateTicket(
            @Parameter(description = "Ticket ID") @PathVariable Integer id,
            @Valid @RequestBody AdminTicketUpdateRequest request) {

        try {
            AdminTicketResponse ticket = adminTicketService.updateTicket(id, request);

            return ResponseEntity.ok(ApiResponse.<AdminTicketResponse>builder()
                    .success(true)
                    .message("Ticket updated successfully")
                    .data(ticket)
                    .build());

        } catch (Exception e) {
            log.error("Error updating ticket {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<AdminTicketResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("UPDATE_FAILED")
                    .build());
        }
    }

    @Operation(summary = "Delete ticket", description = "Delete ticket from database")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(
            @Parameter(description = "Ticket ID") @PathVariable Integer id) {

        try {
            adminTicketService.deleteTicket(id);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Ticket deleted successfully")
                    .build());

        } catch (Exception e) {
            log.error("Error deleting ticket {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("DELETE_FAILED")
                    .build());
        }
    }

    @Operation(summary = "Get ticket statistics", description = "Get ticket count statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Long>> getTicketStatistics() {

        try {
            long count = adminTicketService.countTicketsByStatus();

            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("Statistics retrieved successfully")
                    .data(count)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving ticket statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Long>builder()
                    .success(false)
                    .message("Failed to retrieve statistics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get monthly ticket statistics", description = "Get monthly ticket statistics for a specific year including ticket counts by status and revenue")
    @GetMapping("/statistics/monthly")
    public ResponseEntity<ApiResponse<MonthlyTicketStatisticsResponse>> getMonthlyTicketStatistics(
            @Parameter(description = "Year for statistics", example = "2025") @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year) {

        try {
            // Validate year parameter
            if (year < 2020 || year > 2030) {
                return ResponseEntity.badRequest().body(ApiResponse.<MonthlyTicketStatisticsResponse>builder()
                        .success(false)
                        .message("Year must be between 2020 and 2030")
                        .errors("INVALID_YEAR")
                        .build());
            }

            MonthlyTicketStatisticsRequest request = MonthlyTicketStatisticsRequest.builder()
                    .year(year)
                    .build();

            MonthlyTicketStatisticsResponse statistics = adminTicketService.getMonthlyTicketStatistics(request);

            return ResponseEntity.ok(ApiResponse.<MonthlyTicketStatisticsResponse>builder()
                    .success(true)
                    .message("Monthly ticket statistics retrieved successfully")
                    .data(statistics)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving monthly ticket statistics for year {}: {}", year, e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<MonthlyTicketStatisticsResponse>builder()
                    .success(false)
                    .message("Failed to retrieve monthly ticket statistics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }
}
