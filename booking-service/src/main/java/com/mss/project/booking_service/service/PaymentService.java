package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.payment.MockWebhookRequest;
import com.mss.project.booking_service.payload.payment.PaymentLinkRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentResponse;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

public interface PaymentService {
    CheckoutResponseData createPaymentLink(PaymentLinkRequest req) throws Exception;

    PaymentLinkData getPaymentLinkInformation(Long orderId) throws Exception;

    PaymentLinkData cancelPaymentLink(long orderId, String cancellationReason) throws Exception;

    String confirmWebhook(String webhookUrl) throws Exception;

    WebhookData verifyPaymentWebhookData(Webhook webhookBody) throws Exception;

    WebhookData mockVerifyPaymentWebhookData(MockWebhookRequest webhookBody) throws Exception;

    // Refund methods
    RefundPaymentResponse createRefundPayment(RefundPaymentRequest request) throws Exception;

    RefundPaymentResponse processRefund(Long refundPaymentId) throws Exception;
}
