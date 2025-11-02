package com.mss.project.booking_service.repository;

import com.mss.project.booking_service.entities.Payment;
import com.mss.project.booking_service.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
        Payment getPaymentById(long orderId);

        // Regular payment queries (non-refund payments only)
        @Query("SELECT p FROM Payment p WHERE p.isRefund = false " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:bookingId IS NULL OR p.booking.id = :bookingId) " +
                        "AND (:minAmount IS NULL OR p.amount >= :minAmount) " +
                        "AND (:maxAmount IS NULL OR p.amount <= :maxAmount) " +
                        "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR p.createdAt <= :endDate)")
        Page<Payment> findRegularPaymentsWithFilters(@Param("status") PaymentStatus status,
                        @Param("bookingId") Long bookingId,
                        @Param("minAmount") Integer minAmount,
                        @Param("maxAmount") Integer maxAmount,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.isRefund = false")
        long countRegularPaymentsByStatus(@Param("status") PaymentStatus status);

        @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.isRefund = false")
        Optional<Payment> findRegularPaymentById(@Param("id") Long id);

        @Query(value = "SELECT DATE(p.created_at) as date, " +
                        "COUNT(p.id) as paymentCount, " +
                        "COALESCE(SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END), 0) as totalAmount, " +
                        "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedPayments, " +
                        "SUM(CASE WHEN p.status = 'PENDING' THEN 1 ELSE 0 END) as pendingPayments, " +
                        "SUM(CASE WHEN p.status = 'FAILED' THEN 1 ELSE 0 END) as failedPayments, " +
                        "SUM(CASE WHEN p.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledPayments " +
                        "FROM payments p " +
                        "WHERE p.is_refund = false " +
                        "AND p.created_at >= :startDate " +
                        "AND p.created_at <= :endDate " +
                        "GROUP BY DATE(p.created_at) " +
                        "ORDER BY DATE(p.created_at)", nativeQuery = true)
        List<Object[]> findDailyRegularPaymentStatistics(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query(value = "SELECT " +
                        "COALESCE(SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END), 0) as totalRevenue, "
                        +
                        "COUNT(p.id) as totalPayments, " +
                        "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedPayments " +
                        "FROM payments p " +
                        "WHERE p.is_refund = false " +
                        "AND p.created_at >= :startDate " +
                        "AND p.created_at <= :endDate", nativeQuery = true)
        Object[] findMonthlyRegularPaymentData(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // Refund payment queries
        @Query("SELECT p FROM Payment p WHERE p.isRefund = true " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:bookingId IS NULL OR p.booking.id = :bookingId) " +
                        "AND (:minAmount IS NULL OR ABS(p.refundAmount) >= :minAmount) " +
                        "AND (:maxAmount IS NULL OR ABS(p.refundAmount) <= :maxAmount) " +
                        "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR p.createdAt <= :endDate)")
        Page<Payment> findRefundPaymentsWithFilters(@Param("status") PaymentStatus status,
                        @Param("bookingId") Long bookingId,
                        @Param("minAmount") Integer minAmount,
                        @Param("maxAmount") Integer maxAmount,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query("SELECT p FROM Payment p WHERE p.originalPaymentId = :paymentId AND p.isRefund = true")
        List<Payment> findRefundsByOriginalPaymentId(@Param("paymentId") Long paymentId);

        @Query(value = "SELECT DATE(p.created_at) as date, " +
                        "COUNT(p.id) as refundCount, " +
                        "COALESCE(SUM(CASE WHEN p.status = 'COMPLETED' THEN p.refund_amount ELSE 0 END), 0) as totalRefundAmount, "
                        +
                        "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedRefunds, " +
                        "SUM(CASE WHEN p.status = 'PENDING' THEN 1 ELSE 0 END) as pendingRefunds, " +
                        "SUM(CASE WHEN p.status = 'PROCESSING' THEN 1 ELSE 0 END) as processingRefunds, " +
                        "SUM(CASE WHEN p.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolvedRefunds " +
                        "FROM payments p " +
                        "WHERE p.is_refund = true " +
                        "AND p.created_at >= :startDate " +
                        "AND p.created_at <= :endDate " +
                        "GROUP BY DATE(p.created_at) " +
                        "ORDER BY DATE(p.created_at)", nativeQuery = true)
        List<Object[]> findDailyRefundStatistics(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query(value = "SELECT " +
                        "COALESCE(SUM(CASE WHEN p.status = 'COMPLETED' THEN p.refund_amount ELSE 0 END), 0) as totalRefundAmount, "
                        +
                        "COUNT(p.id) as totalRefunds, " +
                        "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedRefunds " +
                        "FROM payments p " +
                        "WHERE p.is_refund = true " +
                        "AND p.created_at >= :startDate " +
                        "AND p.created_at <= :endDate", nativeQuery = true)
        Object[] findMonthlyRefundData(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // Find all refund payments by original payment ID
        @Query("SELECT p FROM Payment p WHERE p.originalPaymentId = :originalPaymentId AND p.isRefund = true ORDER BY p.createdAt DESC")
        List<Payment> findRefundPaymentsByOriginalPaymentId(@Param("originalPaymentId") Long originalPaymentId);

        // Find all refund payments for a specific user
        @Query("SELECT p FROM Payment p JOIN p.booking b WHERE b.userId = :userId AND p.isRefund = true ORDER BY p.createdAt DESC")
        List<Payment> findRefundPaymentsByUserId(@Param("userId") Long userId);

        // Find refund payments for a specific user with filtering and pagination
        @Query("SELECT p FROM Payment p JOIN p.booking b WHERE b.userId = :userId AND p.isRefund = true " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:createdFromDate IS NULL OR DATE(p.createdAt) >= :createdFromDate) " +
                        "AND (:createdToDate IS NULL OR DATE(p.createdAt) <= :createdToDate) " +
                        "AND (:processedFromDate IS NULL OR DATE(p.updatedAt) >= :processedFromDate) " +
                        "AND (:processedToDate IS NULL OR DATE(p.updatedAt) <= :processedToDate) " +
                        "AND (:minAmount IS NULL OR p.amount >= :minAmount) " +
                        "AND (:maxAmount IS NULL OR p.amount <= :maxAmount) " +
                        "AND (:bookingCode IS NULL OR b.orderCode = :bookingCodeAsLong) " +
                        "AND (:searchKeyword IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) "
                        +
                        "ORDER BY " +
                        "CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'ASC' THEN p.createdAt END ASC, " +
                        "CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'DESC' THEN p.createdAt END DESC, " +
                        "CASE WHEN :sortBy = 'processedAt' AND :sortDirection = 'ASC' THEN p.updatedAt END ASC, " +
                        "CASE WHEN :sortBy = 'processedAt' AND :sortDirection = 'DESC' THEN p.updatedAt END DESC, " +
                        "CASE WHEN :sortBy = 'amount' AND :sortDirection = 'ASC' THEN p.amount END ASC, " +
                        "CASE WHEN :sortBy = 'amount' AND :sortDirection = 'DESC' THEN p.amount END DESC, " +
                        "CASE WHEN :sortBy = 'status' AND :sortDirection = 'ASC' THEN p.status END ASC, " +
                        "CASE WHEN :sortBy = 'status' AND :sortDirection = 'DESC' THEN p.status END DESC, " +
                        "p.createdAt DESC")
        Page<Payment> findRefundPaymentsByUserIdWithFilter(
                        @Param("userId") Long userId,
                        @Param("status") PaymentStatus status,
                        @Param("createdFromDate") LocalDate createdFromDate,
                        @Param("createdToDate") LocalDate createdToDate,
                        @Param("processedFromDate") LocalDate processedFromDate,
                        @Param("processedToDate") LocalDate processedToDate,
                        @Param("minAmount") Integer minAmount,
                        @Param("maxAmount") Integer maxAmount,
                        @Param("bookingCode") String bookingCode,
                        @Param("bookingCodeAsLong") Long bookingCodeAsLong,
                        @Param("searchKeyword") String searchKeyword,
                        @Param("sortBy") String sortBy,
                        @Param("sortDirection") String sortDirection,
                        Pageable pageable);
}
