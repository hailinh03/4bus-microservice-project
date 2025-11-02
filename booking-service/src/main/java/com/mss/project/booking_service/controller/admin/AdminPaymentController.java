package com.mss.project.booking_service.controller.admin;

import com.mss.project.booking_service.enums.PaymentStatus;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentListRequest;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentResponse;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentUpdateRequest;
import com.mss.project.booking_service.payload.admin.payment.DailyPaymentStatisticsRequest;
import com.mss.project.booking_service.payload.admin.payment.DailyPaymentStatisticsResponse;
import com.mss.project.booking_service.payload.admin.payment.MonthlyRevenueRequest;
import com.mss.project.booking_service.payload.admin.payment.MonthlyRevenueResponse;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentResponse;
import com.mss.project.booking_service.payload.payment.RefundPaymentDetailResponse;
import com.mss.project.booking_service.payload.payment.ProcessRefundRequest;
import com.mss.project.booking_service.service.AdminPaymentService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment Management", description = "Admin APIs for payment management")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @Operation(summary = "Get all regular payments with filters", description = "Retrieve paginated list of all regular (non-refund) payments with optional filters")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminPaymentResponse>>> getAllPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Filter by payment status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) Long bookingId,
            @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) Integer minAmount,
            @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) Integer maxAmount,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {

        try {
            AdminPaymentListRequest request = AdminPaymentListRequest.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .bookingId(bookingId)
                    .minAmount(minAmount)
                    .maxAmount(maxAmount)
                    .build();

            // Handle status enum conversion
            if (status != null && !status.isEmpty()) {
                try {
                    request.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                            .success(false)
                            .message("Invalid payment status: " + status)
                            .errors("INVALID_STATUS")
                            .build());
                }
            }

            // Handle date parsing
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    request.setStartDate(LocalDate.parse(startDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                            .success(false)
                            .message("Invalid start date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            if (endDate != null && !endDate.isEmpty()) {
                try {
                    request.setEndDate(LocalDate.parse(endDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                            .success(false)
                            .message("Invalid end date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            Page<AdminPaymentResponse> payments = adminPaymentService.getAllPayments(request);

            return ResponseEntity.ok(ApiResponse.<Page<AdminPaymentResponse>>builder()
                    .success(true)
                    .message("Payments retrieved successfully")
                    .data(payments)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving payments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve payments")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPaymentResponse>> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long id) {

        try {
            AdminPaymentResponse payment = adminPaymentService.getPaymentById(id);

            return ResponseEntity.ok(ApiResponse.<AdminPaymentResponse>builder()
                    .success(true)
                    .message("Payment retrieved successfully")
                    .data(payment)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving payment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(404).body(ApiResponse.<AdminPaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("PAYMENT_NOT_FOUND")
                    .build());
        }
    }

    @Operation(summary = "Update payment", description = "Update payment status and details")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPaymentResponse>> updatePayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Valid @RequestBody AdminPaymentUpdateRequest request) {

        try {
            AdminPaymentResponse payment = adminPaymentService.updatePayment(id, request);

            return ResponseEntity.ok(ApiResponse.<AdminPaymentResponse>builder()
                    .success(true)
                    .message("Payment updated successfully")
                    .data(payment)
                    .build());

        } catch (Exception e) {
            log.error("Error updating payment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<AdminPaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("UPDATE_FAILED")
                    .build());
        }
    }

    // @Operation(summary = "Soft delete payment", description = "Mark payment as
    // deleted without removing from database")
    // @DeleteMapping("/{id}")
    // public ResponseEntity<ApiResponse<Void>> softDeletePayment(
    // @Parameter(description = "Payment ID") @PathVariable Long id) {
    //
    // try {
    // adminPaymentService.softDeletePayment(id);
    //
    // return ResponseEntity.ok(ApiResponse.<Void>builder()
    // .success(true)
    // .message("Payment deleted successfully")
    // .build());
    //
    // } catch (Exception e) {
    // log.error("Error deleting payment {}: {}", id, e.getMessage(), e);
    // return ResponseEntity.status(400).body(ApiResponse.<Void>builder()
    // .success(false)
    // .message(e.getMessage())
    // .errors("DELETE_FAILED")
    // .build());
    // }
    // }

    // @Operation(summary = "Restore payment", description = "Restore a soft-deleted
    // payment")
    // @PostMapping("/{id}/restore")
    // public ResponseEntity<ApiResponse<Void>> restorePayment(
    // @Parameter(description = "Payment ID") @PathVariable Long id) {
    //
    // try {
    // adminPaymentService.restorePayment(id);
    //
    // return ResponseEntity.ok(ApiResponse.<Void>builder()
    // .success(true)
    // .message("Payment restored successfully")
    // .build());
    //
    // } catch (Exception e) {
    // log.error("Error restoring payment {}: {}", id, e.getMessage(), e);
    // return ResponseEntity.status(400).body(ApiResponse.<Void>builder()
    // .success(false)
    // .message(e.getMessage())
    // .errors("RESTORE_FAILED")
    // .build());
    // }
    // }

    @Operation(summary = "Get payment statistics", description = "Get payment count statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Long>> getPaymentStatistics() {

        try {
            long count = adminPaymentService.countPaymentsByStatus();

            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("Statistics retrieved successfully")
                    .data(count)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving payment statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Long>builder()
                    .success(false)
                    .message("Failed to retrieve statistics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get daily payment statistics", description = "Get daily payment statistics with filters for different time periods")
    @GetMapping("/statistics/daily")
    public ResponseEntity<ApiResponse<DailyPaymentStatisticsResponse>> getDailyPaymentStatistics(
            @Parameter(description = "Time period filter", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                    "LAST_7_DAYS", "LAST_30_DAYS",
                    "LAST_3_MONTHS" })) @RequestParam(defaultValue = "LAST_30_DAYS") String period) {

        try {
            DailyPaymentStatisticsRequest request = DailyPaymentStatisticsRequest.builder()
                    .period(period)
                    .build();

            DailyPaymentStatisticsResponse statistics = adminPaymentService.getDailyPaymentStatistics(request);

            return ResponseEntity.ok(ApiResponse.<DailyPaymentStatisticsResponse>builder()
                    .success(true)
                    .message("Daily payment statistics retrieved successfully")
                    .data(statistics)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving daily payment statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<DailyPaymentStatisticsResponse>builder()
                    .success(false)
                    .message("Failed to retrieve daily payment statistics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get monthly revenue comparison", description = "Get current month revenue and percentage change compared to previous month")
    @GetMapping("/revenue/monthly")
    public ResponseEntity<ApiResponse<MonthlyRevenueResponse>> getMonthlyRevenueComparison() {

        try {
            MonthlyRevenueRequest request = new MonthlyRevenueRequest();
            MonthlyRevenueResponse revenue = adminPaymentService.getMonthlyRevenueComparison(request);

            return ResponseEntity.ok(ApiResponse.<MonthlyRevenueResponse>builder()
                    .success(true)
                    .message("Monthly revenue comparison retrieved successfully")
                    .data(revenue)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving monthly revenue comparison: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<MonthlyRevenueResponse>builder()
                    .success(false)
                    .message("Failed to retrieve monthly revenue comparison")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Create refund payment", description = "Create a refund payment for a completed payment")
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundPaymentResponse>> createRefundPayment(
            @Valid @RequestBody RefundPaymentRequest request) {

        try {
            RefundPaymentResponse response = adminPaymentService.createRefundPayment(request);

            return ResponseEntity.ok(ApiResponse.<RefundPaymentResponse>builder()
                    .success(true)
                    .message("Refund payment created successfully")
                    .data(response)
                    .build());

        } catch (Exception e) {
            log.error("Error creating refund payment: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<RefundPaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("REFUND_CREATION_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Process refund payment", description = "Process a refund payment that is in PROCESSING status with proof images")
    @PostMapping("/refund/process")
    public ResponseEntity<ApiResponse<RefundPaymentResponse>> processRefund(
            @Valid @RequestBody ProcessRefundRequest request) {

        try {
            RefundPaymentResponse response = adminPaymentService.processRefund(request);

            return ResponseEntity.ok(ApiResponse.<RefundPaymentResponse>builder()
                    .success(true)
                    .message("Refund processed successfully")
                    .data(response)
                    .build());

        } catch (Exception e) {
            log.error("Error processing refund {}: {}", request.getRefundPaymentId(), e.getMessage(), e);
            return ResponseEntity.status(400).body(ApiResponse.<RefundPaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("REFUND_PROCESSING_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get refund payment by ID", description = "Retrieve detailed information about a specific refund payment including original payment details")
    @GetMapping("/refund/{refundPaymentId}")
    public ResponseEntity<ApiResponse<RefundPaymentDetailResponse>> getRefundPaymentById(
            @Parameter(description = "Refund Payment ID") @PathVariable Long refundPaymentId) {

        try {
            RefundPaymentDetailResponse response = adminPaymentService.getRefundPaymentById(refundPaymentId);

            return ResponseEntity.ok(ApiResponse.<RefundPaymentDetailResponse>builder()
                    .success(true)
                    .message("Refund payment detail retrieved successfully")
                    .data(response)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving refund payment detail {}: {}", refundPaymentId, e.getMessage(), e);
            return ResponseEntity.status(404).body(ApiResponse.<RefundPaymentDetailResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("REFUND_PAYMENT_NOT_FOUND")
                    .build());
        }
    }

    @Operation(summary = "Get refund payments by original payment ID", description = "Retrieve all refund payments associated with a specific original payment")
    @GetMapping("/payment/{originalPaymentId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundPaymentResponse>>> getRefundPaymentsByOriginalPaymentId(
            @Parameter(description = "Original Payment ID") @PathVariable Long originalPaymentId) {

        try {
            List<RefundPaymentResponse> refunds = adminPaymentService
                    .getRefundPaymentsByOriginalPaymentId(originalPaymentId);

            return ResponseEntity.ok(ApiResponse.<List<RefundPaymentResponse>>builder()
                    .success(true)
                    .message("Refund payments retrieved successfully")
                    .data(refunds)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving refund payments for original payment {}: {}", originalPaymentId, e.getMessage(),
                    e);
            return ResponseEntity.status(404).body(ApiResponse.<List<RefundPaymentResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("ORIGINAL_PAYMENT_NOT_FOUND")
                    .build());
        }
    }

    @Operation(summary = "Get refund payments", description = "Retrieve paginated list of refund payments with optional filters")
    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<Page<AdminPaymentResponse>>> getRefundPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Filter by payment status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) Long bookingId,
            @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) Integer minAmount,
            @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) Integer maxAmount,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            @Parameter(description = "Include soft deleted payments") @RequestParam(defaultValue = "false") Boolean includeDeleted) {

        try {
            AdminPaymentListRequest request = AdminPaymentListRequest.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .bookingId(bookingId)
                    .minAmount(minAmount)
                    .maxAmount(maxAmount)
                    .build();

            // Handle status enum conversion
            if (status != null && !status.isEmpty()) {
                try {
                    request.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                            .success(false)
                            .message("Invalid payment status: " + status)
                            .errors("INVALID_STATUS")
                            .build());
                }
            }

            // Handle date parsing
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    request.setStartDate(LocalDate.parse(startDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                            .success(false)
                            .message("Invalid start date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            if (endDate != null && !endDate.isEmpty()) {
                try {
                    request.setEndDate(LocalDate.parse(endDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                            .success(false)
                            .message("Invalid end date format. Use yyyy-MM-dd")
                            .errors("INVALID_DATE_FORMAT")
                            .build());
                }
            }

            Page<AdminPaymentResponse> refundPayments = adminPaymentService.getRefundPayments(request);

            return ResponseEntity.ok(ApiResponse.<Page<AdminPaymentResponse>>builder()
                    .success(true)
                    .message("Refund payments retrieved successfully")
                    .data(refundPayments)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving refund payments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<Page<AdminPaymentResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve refund payments")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }
}
