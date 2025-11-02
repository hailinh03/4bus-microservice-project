package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.PagedResponse;
import com.mss.project.booking_service.payload.user.payment.UserRefundPaymentFilter;
import com.mss.project.booking_service.payload.user.payment.UserRefundPaymentResponse;

import java.util.List;

public interface UserPaymentService {
    /**
     * Get all refund payments for the authenticated user (deprecated - use
     * paginated version)
     * 
     * @param userId The ID of the authenticated user
     * @return List of user's refund payments with comprehensive information
     * @deprecated Use getUserRefundPayments(Long userId, UserRefundPaymentFilter
     *             filter) instead
     */
    @Deprecated
    List<UserRefundPaymentResponse> getUserRefundPayments(Long userId);

    /**
     * Get refund payments for the authenticated user with pagination and filtering
     * 
     * @param userId The ID of the authenticated user
     * @param filter Filter and pagination parameters
     * @return Paginated list of user's refund payments with comprehensive
     *         information
     */
    PagedResponse<UserRefundPaymentResponse> getUserRefundPayments(Long userId, UserRefundPaymentFilter filter);

    /**
     * Get a specific refund payment by ID for the authenticated user
     * 
     * @param userId          The ID of the authenticated user
     * @param refundPaymentId The ID of the refund payment
     * @return Detailed refund payment information
     */
    UserRefundPaymentResponse getUserRefundPaymentById(Long userId, Long refundPaymentId);
}
