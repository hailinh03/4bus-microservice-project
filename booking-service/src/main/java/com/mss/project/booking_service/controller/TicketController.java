package com.mss.project.booking_service.controller;

import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.ticket.CancelTicketRequest;
import com.mss.project.booking_service.payload.ticket.CancelTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import com.mss.project.booking_service.service.TicketService;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Ticket Management", description = "APIs for ticket management")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/booked-seats/{tripId}")
    public ResponseEntity<ApiResponse<List<String>>> getBookedSeatsByTripId(@PathVariable Integer tripId) {
        List<String> response = ticketService.getBookedSeatsByTripId(tripId);
        ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
                .message("Get booked seats by trip ID successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Cancel a ticket", description = "Cancel an active ticket owned by the authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{ticketId}/cancel")
    public ResponseEntity<ApiResponse<CancelTicketResponse>> cancelTicket(
            @Parameter(description = "Ticket ID to cancel") @PathVariable Integer ticketId,
            @RequestBody CancelTicketRequest request) {
        CancelTicketResponse response = ticketService.cancelTicket(ticketId, request);
        ApiResponse<CancelTicketResponse> apiResponse = ApiResponse.<CancelTicketResponse>builder()
                .message("Ticket cancelled successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Mark all tickets as USED for a trip", description = "Set all ACTIVE tickets of a trip to USED status")
    @PutMapping("/mark-used/{tripId}")
    public ResponseEntity<ApiResponse<Void>> markTicketsUsedByTripId(@PathVariable Integer tripId) {
        try {
            ticketService.markTicketsUsedByTripId(tripId);
            ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                    .success(true)
                    .message("All ACTIVE tickets for trip marked as USED successfully")
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Failed to mark tickets as USED")
                    .errors(e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    @Operation(summary = "Mark all tickets as EXPIRED for a trip", description = "Set all ACTIVE tickets of a trip to EXPIRED status")
    @PutMapping("/mark-expired/{tripId}")
    public ResponseEntity<ApiResponse<Void>> markTicketsExpiredByTripId(@PathVariable Integer tripId) {
        try {
            ticketService.markTicketsExpiredByTripId(tripId);
            ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                    .success(true)
                    .message("All ACTIVE tickets for trip marked as EXPIRED successfully")
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Failed to mark tickets as EXPIRED")
                    .errors(e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }
}
