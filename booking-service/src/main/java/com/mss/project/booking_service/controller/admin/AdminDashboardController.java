package com.mss.project.booking_service.controller.admin;

import com.mss.project.booking_service.payload.admin.dashboard.DashboardAnalyticsRequest;
import com.mss.project.booking_service.payload.admin.dashboard.DashboardOverviewResponse;
import com.mss.project.booking_service.payload.admin.dashboard.PaymentAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.RefundAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.RevenueAnalyticsResponse;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.service.AdminPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Admin Dashboard Analytics APIs")
public class AdminDashboardController {

    private final AdminPaymentService adminPaymentService;

    @Operation(summary = "Get dashboard overview", description = "Get comprehensive dashboard overview with key metrics for payments, bookings, tickets, and refunds")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getDashboardOverview(
            @Parameter(description = "Time period filter", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                    "LAST_7_DAYS", "LAST_30_DAYS", "LAST_3_MONTHS", "LAST_6_MONTHS", "LAST_YEAR",
                    "CUSTOM" })) @RequestParam(defaultValue = "LAST_30_DAYS") String period,

            @Parameter(description = "Start date for CUSTOM period (yyyy-MM-dd format)") @RequestParam(required = false) String startDate,

            @Parameter(description = "End date for CUSTOM period (yyyy-MM-dd format)") @RequestParam(required = false) String endDate,

            @Parameter(description = "Include refunds in calculations") @RequestParam(defaultValue = "true") Boolean includeRefunds,

            @Parameter(description = "Include projections in response") @RequestParam(defaultValue = "false") Boolean includeProjections) {

        try {
            DashboardAnalyticsRequest request = DashboardAnalyticsRequest.builder()
                    .period(period)
                    .startDate(startDate)
                    .endDate(endDate)
                    .includeRefunds(includeRefunds)
                    .includeProjections(includeProjections)
                    .build();

            DashboardOverviewResponse overview = adminPaymentService.getDashboardOverview(request);

            return ResponseEntity.ok(ApiResponse.<DashboardOverviewResponse>builder()
                    .success(true)
                    .message("Dashboard overview retrieved successfully")
                    .data(overview)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving dashboard overview: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<DashboardOverviewResponse>builder()
                    .success(false)
                    .message("Failed to retrieve dashboard overview")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get payment analytics", description = "Get detailed payment analytics including trends, status breakdown, and revenue metrics")
    @PostMapping("/analytics/payments")
    public ResponseEntity<ApiResponse<PaymentAnalyticsResponse>> getPaymentAnalytics(
            @Valid @RequestBody DashboardAnalyticsRequest request) {

        try {
            PaymentAnalyticsResponse analytics = adminPaymentService.getPaymentAnalytics(request);

            return ResponseEntity.ok(ApiResponse.<PaymentAnalyticsResponse>builder()
                    .success(true)
                    .message("Payment analytics retrieved successfully")
                    .data(analytics)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving payment analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<PaymentAnalyticsResponse>builder()
                    .success(false)
                    .message("Failed to retrieve payment analytics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get refund analytics", description = "Get comprehensive refund analytics including refund trends, processing metrics, and refund rates")
    @PostMapping("/analytics/refunds")
    public ResponseEntity<ApiResponse<RefundAnalyticsResponse>> getRefundAnalytics(
            @Valid @RequestBody DashboardAnalyticsRequest request) {

        try {
            RefundAnalyticsResponse analytics = adminPaymentService.getRefundAnalytics(request);

            return ResponseEntity.ok(ApiResponse.<RefundAnalyticsResponse>builder()
                    .success(true)
                    .message("Refund analytics retrieved successfully")
                    .data(analytics)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving refund analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<RefundAnalyticsResponse>builder()
                    .success(false)
                    .message("Failed to retrieve refund analytics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get revenue analytics", description = "Get detailed revenue analytics including revenue trends, projections, and breakdown by time periods")
    @PostMapping("/analytics/revenue")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @Valid @RequestBody DashboardAnalyticsRequest request) {

        try {
            RevenueAnalyticsResponse analytics = adminPaymentService.getRevenueAnalytics(request);

            return ResponseEntity.ok(ApiResponse.<RevenueAnalyticsResponse>builder()
                    .success(true)
                    .message("Revenue analytics retrieved successfully")
                    .data(analytics)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving revenue analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<RevenueAnalyticsResponse>builder()
                    .success(false)
                    .message("Failed to retrieve revenue analytics")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get quick stats", description = "Get quick dashboard statistics for real-time monitoring")
    @GetMapping("/quick-stats")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getQuickStats() {

        try {
            DashboardAnalyticsRequest request = DashboardAnalyticsRequest.builder()
                    .period("LAST_7_DAYS")
                    .includeRefunds(true)
                    .includeProjections(false)
                    .build();

            DashboardOverviewResponse overview = adminPaymentService.getDashboardOverview(request);

            return ResponseEntity.ok(ApiResponse.<DashboardOverviewResponse>builder()
                    .success(true)
                    .message("Quick stats retrieved successfully")
                    .data(overview)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving quick stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<DashboardOverviewResponse>builder()
                    .success(false)
                    .message("Failed to retrieve quick stats")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }
}
