package com.mss.project.booking_service.controller;

import com.mss.project.booking_service.exception.PaymentLinkException;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.payment.CancelPaymentLinkRequest;
import com.mss.project.booking_service.payload.payment.MockWebhookRequest;
import com.mss.project.booking_service.payload.payment.PaymentLinkRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentResponse;
import com.mss.project.booking_service.payload.payment.WebhookUrlRequest;
import com.mss.project.booking_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.Webhook;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Management", description = "APIs for payment processing")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Object>> getPaymentLinkInformation(@PathVariable Long orderId) {
        try {
            var res = paymentService.getPaymentLinkInformation(orderId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Fetched payment link info")
                    .data(res)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to fetch payment link info")
                    .errors("PAYMENT_LINK_INFO_ERROR")
                    .build());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelPaymentLink(@RequestBody CancelPaymentLinkRequest req) {
        try {
            var res = paymentService.cancelPaymentLink(req.getOrderCode(), req.getCancellationReason());
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Cancelled payment link")
                    .data(res)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to cancel payment link")
                    .errors("CANCEL_PAYMENT_LINK_ERROR")
                    .build());
        }
    }

    @PostMapping("/webhook/confirm")
    public ResponseEntity<ApiResponse<Object>> confirmWebhook(@RequestBody WebhookUrlRequest req) {
        try {
            var res = paymentService.confirmWebhook(req.getWebhookUrl());
            return ResponseEntity
                    .ok(ApiResponse.builder().success(true).message("Webhook confirmed").data(res).build());
        } catch (Exception e) {
            throw new PaymentLinkException("Xác nhận webhook thất bại: " + e.getMessage());
        }
    }

    // @PostMapping("/webhook/verify")
    // public ResponseEntity<ApiResponse<Object>>
    // verifyPaymentWebhookData(@RequestBody Webhook req) {
    // try {
    // var res = paymentService.verifyPaymentWebhookData(req);
    // return ResponseEntity.ok(ApiResponse.builder().success(true).message("Webhook
    // verified").data(res).build());
    // } catch (Exception e) {
    // throw new PaymentLinkException("Xác thực webhook thất bại: " +
    // e.getMessage());
    // }
    // }

    @PostMapping("/webhook/verify")
    public ResponseEntity<Map<String, Boolean>> verifyPaymentWebhookData(@RequestBody Webhook req) {
        try {
            var res = paymentService.verifyPaymentWebhookData(req);
            // return ResponseEntity.ok(ApiResponse.builder().success(true).message("Webhook
            // verified").data(res).build());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            // throw new PaymentLinkException("Xác thực webhook thất bại: " +
            // e.getMessage());
            return ResponseEntity.ok(Map.of("success", false));
        }
    }

    @PostMapping("/create-link")
    public ResponseEntity<ApiResponse<Object>> createPaymentLink(@RequestBody PaymentLinkRequest req) {
        try {

            CheckoutResponseData res = paymentService.createPaymentLink(req);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Created payment link")
                    .data(res)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to create payment link")
                    .errors("CREATE_PAYMENT_LINK_ERROR")
                    .build());
        }
    }

    @PostMapping("/webhook/mock-verify")
    public ResponseEntity<ApiResponse<Object>> mockVerifyPaymentWebhookData(@RequestBody MockWebhookRequest req) {
        try {
            var res = paymentService.mockVerifyPaymentWebhookData(req);
            return ResponseEntity
                    .ok(ApiResponse.builder().success(true).message("Mock webhook verified").data(res).build());
        } catch (Exception e) {
            throw new PaymentLinkException("Xác thực webhook thất bại: " + e.getMessage());
        }
    }

    @Operation(summary = "Create refund payment", description = "Create a refund payment for a completed payment")
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<RefundPaymentResponse>> createRefundPayment(
            @Valid @RequestBody RefundPaymentRequest request) {
        try {
            RefundPaymentResponse response = paymentService.createRefundPayment(request);
            return ResponseEntity.ok(ApiResponse.<RefundPaymentResponse>builder()
                    .success(true)
                    .message("Refund payment created successfully")
                    .data(response)
                    .build());
        } catch (PaymentLinkException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<RefundPaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("REFUND_CREATION_ERROR")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<RefundPaymentResponse>builder()
                    .success(false)
                    .message("Failed to create refund payment")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

    @Operation(summary = "Process refund payment", description = "Process a refund payment that is in PROCESSING status")
    @PostMapping("/refund/{refundPaymentId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<RefundPaymentResponse>> processRefund(
            @Parameter(description = "Refund Payment ID") @PathVariable Long refundPaymentId) {
        try {
            RefundPaymentResponse response = paymentService.processRefund(refundPaymentId);
            return ResponseEntity.ok(ApiResponse.<RefundPaymentResponse>builder()
                    .success(true)
                    .message("Refund processed successfully")
                    .data(response)
                    .build());
        } catch (PaymentLinkException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<RefundPaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .errors("REFUND_PROCESSING_ERROR")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<RefundPaymentResponse>builder()
                    .success(false)
                    .message("Failed to process refund")
                    .errors("INTERNAL_SERVER_ERROR")
                    .build());
        }
    }

}
