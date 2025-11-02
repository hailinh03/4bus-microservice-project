package com.mss.project.booking_service.repository;

import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
        Optional<Booking> findByOrderCode(Long orderCode);

        // Admin queries
        @Query("SELECT b FROM Booking b WHERE " +
                        "(:status IS NULL OR b.status = :status) " +
                        "AND (:userId IS NULL OR b.userId = :userId) " +
                        "AND (:tripId IS NULL OR b.tripId = :tripId) " +
                        "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR b.createdAt <= :endDate)")
        Page<Booking> findAllWithFilters(@Param("status") BookingStatus status,
                        @Param("userId") Long userId,
                        @Param("tripId") Integer tripId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
        long countByStatus(@Param("status") BookingStatus status);

        // User booking history queries
        @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
                        "AND (:status IS NULL OR b.status = :status) " +
                        "AND (:tripId IS NULL OR b.tripId = :tripId) " +
                        "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR b.createdAt <= :endDate)")
        Page<Booking> findBookingHistoryByUserId(@Param("userId") Long userId,
                        @Param("status") BookingStatus status,
                        @Param("tripId") Integer tripId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);
}
