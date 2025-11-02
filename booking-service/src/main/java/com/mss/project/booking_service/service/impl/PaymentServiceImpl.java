package com.mss.project.booking_service.service.impl;

import com.mss.project.booking_service.entities.Payment;
import com.mss.project.booking_service.enums.PaymentStatus;
import com.mss.project.booking_service.enums.TicketStatus;
import com.mss.project.booking_service.exception.PaymentLinkException;
import com.mss.project.booking_service.payload.payment.MockWebhookRequest;
import com.mss.project.booking_service.payload.payment.PaymentLinkRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentResponse;
import com.mss.project.booking_service.payload.ticket.TicketRequest;
import com.mss.project.booking_service.repository.PaymentRepository;
import com.mss.project.booking_service.service.PaymentService;
import com.mss.project.booking_service.service.WebSocketService;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.*;
import org.apache.commons.codec.digest.HmacUtils;
import java.time.Instant;
import java.util.*;

import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.event.PaymentCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    PayOS payOS;
    @Autowired
    private WebSocketService webSocketService;

    @PostConstruct
    public void init() {
        payOS = new PayOS(clientId, apiKey, checksumKey);

    }

    static Long generateOrderCode() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 900);
        String orderCodeStr = String.format("%d%03d", timestamp, random);
        return Long.parseLong(orderCodeStr);
    }

    public CheckoutResponseData createPaymentLink(PaymentLinkRequest req) {
        try {
            Long orderId = generateOrderCode();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderId)
                    .amount(req.getAmount())
                    .description("ORD" + orderId)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .expiredAt(Instant.now().getEpochSecond() + 30 * 60)
                    .items(req.getItems())
                    .build();

            return payOS.createPaymentLink(paymentData);
        } catch (Exception e) {
            throw new PaymentLinkException("Tạo payment link thất bại: " + e.getMessage());
        }
    }

    public PaymentLinkData getPaymentLinkInformation(Long orderId) {
        try {
            return payOS.getPaymentLinkInformation(orderId);
        } catch (Exception e) {
            throw new PaymentLinkException("Lấy thông tin payment link thất bại: " + e.getMessage());
        }
    }

    public PaymentLinkData cancelPaymentLink(long orderId, String cancellationReason) {
        try {
            Payment payment = paymentRepository.getPaymentById(orderId);
            if (payment.getStatus() != PaymentStatus.PENDING) {
                throw new PaymentLinkException("Payment link can only be cancelled if it is in PENDING status");
            }
            payment.setStatus(PaymentStatus.CANCELLED);
            if (cancellationReason != null && !cancellationReason.isEmpty()) {
                payment.setAdminNote(cancellationReason);
            } else {
                payment.setAdminNote("Hủy payment link bởi người dùng");
            }
            paymentRepository.save(payment);
            return payOS.cancelPaymentLink(orderId, cancellationReason);
        } catch (Exception e) {
            throw new PaymentLinkException("Hủy payment link thất bại: " + e.getMessage());
        }
    }

    public String confirmWebhook(String webhookUrl) {
        try {
            return payOS.confirmWebhook(webhookUrl);
        } catch (Exception e) {
            throw new PaymentLinkException("Xác nhận webhook thất bại: " + e.getMessage());
        }
    }

    public WebhookData verifyPaymentWebhookData(Webhook webhookBody) {
        try {
            WebhookData verifiedData = payOS.verifyPaymentWebhookData(webhookBody);

            if (verifiedData != null && "00".equals(verifiedData.getCode())) {
                Long orderCode = verifiedData.getOrderCode();
                Payment payment = paymentRepository.findById(orderCode)
                        .orElseThrow(() -> new PaymentLinkException("Payment not found for order code: " + orderCode));
                if (payment.getStatus() != PaymentStatus.PENDING) {
                    throw new PaymentLinkException("Payment is not in PENDING status, cannot verify webhook");
                }
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                webSocketService.sendMessage("/topic/payment/" + payment.getId().toString(), payment.getId(),
                        payment.getStatus().toString(), "Payment Update");

                Booking booking = bookingRepository.findByOrderCode(orderCode)
                        .orElse(null);

                if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);

                    eventPublisher.publishEvent(
                            new PaymentCompletedEvent(this, booking.getId().longValue(), payment.getId()));
                }
            }
            return verifiedData;
        } catch (Exception e) {
            throw new PaymentLinkException("Xác thực webhook thất bại: " + e.getMessage());
        }
    }

    public WebhookData mockVerifyPaymentWebhookData(MockWebhookRequest webhookBody) {
        try {
            WebhookData verifiedData = WebhookData.builder()
                    .amount(10000)
                    .description("Default Description")
                    .accountNumber("DefaultAccountNumber")
                    .reference("DefaultReference")
                    .transactionDateTime("DefaultDateTime")
                    .currency("VND")
                    .paymentLinkId("DefaultPaymentLinkId")
                    .code("00")
                    .desc("Default Description")
                    .orderCode(webhookBody.getOrderId())
                    .build();
            if (verifiedData != null && "00".equals(verifiedData.getCode())) {
                Long orderCode = verifiedData.getOrderCode();
                Payment payment = paymentRepository.findById(orderCode)
                        .orElseThrow(() -> new PaymentLinkException("Payment not found for order code: " + orderCode));
                if (payment.getStatus() != PaymentStatus.PENDING) {
                    throw new PaymentLinkException("Payment is not in PENDING status, cannot verify webhook");
                }
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                webSocketService.sendMessage("/topic/payment/" + payment.getId().toString(), payment.getId(),
                        payment.getStatus().toString(), "Payment Update");

                Booking booking = bookingRepository.findByOrderCode(orderCode)
                        .orElse(null);

                if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);

                    eventPublisher.publishEvent(
                            new PaymentCompletedEvent(this, booking.getId().longValue(), payment.getId()));
                }
            }
            return verifiedData;
        } catch (Exception e) {
            throw new PaymentLinkException("Xác thực webhook thất bại: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RefundPaymentResponse createRefundPayment(RefundPaymentRequest request) throws Exception {
        try {
            // Find the original payment
            Payment originalPayment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(
                            () -> new PaymentLinkException("Payment not found with id: " + request.getPaymentId()));

            // Validate that the original payment can be refunded
            if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
                throw new PaymentLinkException("Only completed payments can be refunded");
            }

            if (originalPayment.getIsRefund()) {
                throw new PaymentLinkException("Cannot refund a refund payment");
            }

            // Validate refund amount doesn't exceed original amount
            if (request.getRefundAmount() > originalPayment.getAmount()) {
                throw new PaymentLinkException("Refund amount cannot exceed original payment amount");
            }

            // Generate unique order code for refund payment
            Long refundOrderCode = generateOrderCode();

            // Create refund payment record
            Payment refundPayment = Payment.builder()
                    .id(refundOrderCode)
                    .status(PaymentStatus.PROCESSING)
                    .amount(request.getRefundAmount()) // Negative amount for refund
                    .description("REFUND for payment " + request.getPaymentId() + " - " + request.getRefundReason())
                    .booking(originalPayment.getBooking())
                    .isRefund(true)
                    .originalPaymentId(request.getPaymentId())
                    .refundAmount(request.getRefundAmount())
                    .refundReason(request.getRefundReason())
                    .refundRequestedAt(Instant.now())
                    .build();

            Payment savedRefundPayment = paymentRepository.save(refundPayment);

            // Send WebSocket notification
            webSocketService.sendMessage(
                    "/topic/payment/" + savedRefundPayment.getId().toString(),
                    savedRefundPayment.getId(),
                    savedRefundPayment.getStatus().toString(),
                    "Refund Payment Created");

            return mapToRefundResponse(savedRefundPayment);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentLinkException("Failed to create refund payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RefundPaymentResponse processRefund(Long refundPaymentId) throws Exception {
        try {
            // Find the refund payment
            Payment refundPayment = paymentRepository.findById(refundPaymentId)
                    .orElseThrow(
                            () -> new PaymentLinkException("Refund payment not found with id: " + refundPaymentId));

            // Validate that this is a refund payment
            if (!refundPayment.getIsRefund()) {
                throw new PaymentLinkException("Payment is not a refund payment");
            }

            // Validate current status
            if (refundPayment.getStatus() != PaymentStatus.PROCESSING) {
                throw new PaymentLinkException("Refund payment is not in PROCESSING status");
            }

            // Process the refund (in real implementation, this would involve calling
            // payment gateway)
            // For now, we'll simulate processing and mark as resolved
            refundPayment.setStatus(PaymentStatus.RESOLVED);
            refundPayment.setRefundProcessedAt(Instant.now());

            Payment processedRefund = paymentRepository.save(refundPayment);

            // Send WebSocket notification
            webSocketService.sendMessage(
                    "/topic/payment/" + processedRefund.getId().toString(),
                    processedRefund.getId(),
                    processedRefund.getStatus().toString(),
                    "Refund Payment Processed");

            return mapToRefundResponse(processedRefund);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentLinkException("Failed to process refund: " + e.getMessage());
        }
    }

    private RefundPaymentResponse mapToRefundResponse(Payment refundPayment) {
        return RefundPaymentResponse.builder()
                .refundPaymentId(refundPayment.getId())
                .originalPaymentId(refundPayment.getOriginalPaymentId())
                .status(refundPayment.getStatus())
                .refundAmount(refundPayment.getRefundAmount())
                .refundReason(refundPayment.getRefundReason())
                .paymentDate(refundPayment.getPaymentDate())
                .refundRequestedAt(refundPayment.getRefundRequestedAt())
                .refundProcessedAt(refundPayment.getRefundProcessedAt())
                .bookingId(refundPayment.getBooking().getId().longValue())
                .description(refundPayment.getDescription())
                .createdAt(refundPayment.getCreatedAt())
                .updatedAt(refundPayment.getUpdatedAt())
                .adminNote(refundPayment.getAdminNote())
                .build();
    }
}
