package com.mss.project.booking_service.controller.user;

import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.PagedResponse;
import com.mss.project.booking_service.payload.user.payment.UserRefundPaymentFilter;
import com.mss.project.booking_service.payload.user.payment.UserRefundPaymentResponse;
import com.mss.project.booking_service.service.UserPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Payment Management", description = "User APIs for payment and refund information")
@SecurityRequirement(name = "Bearer Authentication")
public class UserPaymentController {

    private final UserPaymentService userPaymentService;

    @Operation(summary = "Get user's refund payments (deprecated)", description = "Retrieve all refund payments for the authenticated user. Use paginated version instead.")
    @GetMapping("/refunds/all")
    @Deprecated
    public ResponseEntity<ApiResponse<List<UserRefundPaymentResponse>>> getUserRefundPaymentsAll() {
        try {
            Long userId = extractUserIdFromAuth();
            log.info("Fetching all refund payments for user: {}", userId);

            List<UserRefundPaymentResponse> refunds = userPaymentService.getUserRefundPayments(userId);

            return ResponseEntity.ok(ApiResponse.<List<UserRefundPaymentResponse>>builder()
                    .success(true)
                    .message("Refund payments retrieved successfully")
                    .data(refunds)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving user refund payments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<List<UserRefundPaymentResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve refund payments")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get user's refund payments with pagination and filtering", description = "Retrieve refund payments for the authenticated user with comprehensive filtering, sorting, and pagination support")
    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<PagedResponse<UserRefundPaymentResponse>>> getUserRefundPayments(
            UserRefundPaymentFilter filter) {
        try {
            Long userId = extractUserIdFromAuth();
            log.info("Fetching paginated refund payments for user: {} with filter: {}", userId, filter);

            PagedResponse<UserRefundPaymentResponse> refunds = userPaymentService.getUserRefundPayments(userId, filter);

            return ResponseEntity.ok(ApiResponse.<PagedResponse<UserRefundPaymentResponse>>builder()
                    .success(true)
                    .message("Refund payments retrieved successfully")
                    .data(refunds)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving paginated user refund payments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.<PagedResponse<UserRefundPaymentResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve refund payments")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Get user's refund payment by ID", description = "Retrieve detailed information about a specific refund payment for the authenticated user")
    @GetMapping("/refunds/{refundPaymentId}")
    public ResponseEntity<ApiResponse<UserRefundPaymentResponse>> getUserRefundPaymentById(
            @Parameter(description = "Refund Payment ID") @PathVariable Long refundPaymentId) {

        try {
            Long userId = extractUserIdFromAuth();
            log.info("Fetching refund payment {} for user: {}", refundPaymentId, userId);

            UserRefundPaymentResponse refund = userPaymentService.getUserRefundPaymentById(userId, refundPaymentId);

            return ResponseEntity.ok(ApiResponse.<UserRefundPaymentResponse>builder()
                    .success(true)
                    .message("Refund payment retrieved successfully")
                    .data(refund)
                    .build());

        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Access denied for refund payment {}: {}", refundPaymentId, e.getMessage());
            return ResponseEntity.status(403).body(ApiResponse.<UserRefundPaymentResponse>builder()
                    .success(false)
                    .message("Access denied. This refund payment does not belong to you")
                    .errors("ACCESS_DENIED")
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving refund payment {} for user: {}", refundPaymentId, e.getMessage(), e);

            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(ApiResponse.<UserRefundPaymentResponse>builder()
                        .success(false)
                        .message(e.getMessage())
                        .errors("REFUND_PAYMENT_NOT_FOUND")
                        .build());
            }

            return ResponseEntity.status(500).body(ApiResponse.<UserRefundPaymentResponse>builder()
                    .success(false)
                    .message("Failed to retrieve refund payment")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    /**
     * Extract user ID from JWT token
     */
    private Long extractUserIdFromAuth() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                // Extract user ID from JWT token
                Object userIdClaim = jwt.getClaim("userId"); // Adjust claim name as needed
                if (userIdClaim != null) {
                    return Long.valueOf(userIdClaim.toString());
                }

                // Fallback to subject if userId claim not available
                String subject = jwt.getSubject();
                if (subject != null) {
                    return Long.valueOf(subject);
                }
            }

            throw new SecurityException("Unable to extract user ID from authentication token");

        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid user ID format in authentication token");
        } catch (Exception e) {
            throw new SecurityException("Authentication error: " + e.getMessage());
        }
    }
}
