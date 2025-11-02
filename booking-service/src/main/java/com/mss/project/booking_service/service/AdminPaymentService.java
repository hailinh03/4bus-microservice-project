package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.admin.payment.AdminPaymentListRequest;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentResponse;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentUpdateRequest;
import com.mss.project.booking_service.payload.admin.payment.DailyPaymentStatisticsRequest;
import com.mss.project.booking_service.payload.admin.payment.DailyPaymentStatisticsResponse;
import com.mss.project.booking_service.payload.admin.payment.MonthlyRevenueRequest;
import com.mss.project.booking_service.payload.admin.payment.MonthlyRevenueResponse;
import com.mss.project.booking_service.payload.admin.dashboard.DashboardOverviewResponse;
import com.mss.project.booking_service.payload.admin.dashboard.PaymentAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.RefundAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.RevenueAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.DashboardAnalyticsRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentResponse;
import com.mss.project.booking_service.payload.payment.RefundPaymentDetailResponse;
import com.mss.project.booking_service.payload.payment.ProcessRefundRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminPaymentService {
    Page<AdminPaymentResponse> getAllPayments(AdminPaymentListRequest request);

    AdminPaymentResponse getPaymentById(Long id);

    AdminPaymentResponse updatePayment(Long id, AdminPaymentUpdateRequest request);

    void deletePayment(Long id);

    long countPaymentsByStatus();

    DailyPaymentStatisticsResponse getDailyPaymentStatistics(DailyPaymentStatisticsRequest request);

    MonthlyRevenueResponse getMonthlyRevenueComparison(MonthlyRevenueRequest request);

    // Refund methods
    RefundPaymentResponse createRefundPayment(RefundPaymentRequest request);

    RefundPaymentResponse processRefund(ProcessRefundRequest request);

    RefundPaymentDetailResponse getRefundPaymentById(Long refundPaymentId);

    List<RefundPaymentResponse> getRefundPaymentsByOriginalPaymentId(Long originalPaymentId);

    Page<AdminPaymentResponse> getRefundPayments(AdminPaymentListRequest request);

    // Dashboard Analytics methods
    DashboardOverviewResponse getDashboardOverview(DashboardAnalyticsRequest request);

    PaymentAnalyticsResponse getPaymentAnalytics(DashboardAnalyticsRequest request);

    RefundAnalyticsResponse getRefundAnalytics(DashboardAnalyticsRequest request);

    RevenueAnalyticsResponse getRevenueAnalytics(DashboardAnalyticsRequest request);
}
