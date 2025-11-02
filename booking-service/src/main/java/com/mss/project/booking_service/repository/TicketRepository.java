package com.mss.project.booking_service.repository;

import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

        @Query("SELECT t.seatId FROM Ticket t WHERE t.tripId = :tripId AND t.status = :ticketStatus")
        List<String> findSeatIdByTripIdAndStatus(@Param("tripId") Integer tripId,
                        @Param("ticketStatus") TicketStatus ticketStatus);

        // Admin queries
        @Query("SELECT t FROM Ticket t WHERE " +
                        "(:status IS NULL OR t.status = :status) " +
                        "AND (:bookingId IS NULL OR t.booking.id = :bookingId) " +
                        "AND (:tripId IS NULL OR t.tripId = :tripId) " +
                        "AND (:seatCode IS NULL OR t.seatCode LIKE %:seatCode%) " +
                        "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR t.createdAt <= :endDate)")
        Page<Ticket> findAllWithFilters(@Param("status") TicketStatus status,
                        @Param("bookingId") Integer bookingId,
                        @Param("tripId") Integer tripId,
                        @Param("seatCode") String seatCode,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
        long countByStatus(@Param("status") TicketStatus status);

        // Find tickets by booking IDs for booking history
        @Query("SELECT t FROM Ticket t WHERE t.booking.id IN :bookingIds")
        List<Ticket> findByBookingIds(@Param("bookingIds") List<Integer> bookingIds);

        // Find tickets by booking ID for booking history
        @Query("SELECT t FROM Ticket t WHERE t.booking.id = :bookingId")
        List<Ticket> findByBookingId(@Param("bookingId") Integer bookingId);

        // Find ticket by ID and validate user ownership
        @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId AND t.booking.userId = :userId")
        Optional<Ticket> findByIdAndUserId(@Param("ticketId") Integer ticketId, @Param("userId") Long userId);

        // Find all tickets by tripId and status for bulk update
        @Query("SELECT t FROM Ticket t WHERE t.tripId = :tripId AND t.status = :status")
        List<Ticket> findByTripIdAndStatus(@Param("tripId") Integer tripId, @Param("status") TicketStatus status);

        // Monthly ticket statistics query
        @Query(value = "SELECT " +
                        "MONTH(t.created_at) as month, " +
                        "COUNT(t.id) as totalTickets, " +
                        "SUM(CASE WHEN t.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeTickets, " +
                        "SUM(CASE WHEN t.status = 'USED' THEN 1 ELSE 0 END) as usedTickets, " +
                        "SUM(CASE WHEN t.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledTickets, " +
                        "SUM(CASE WHEN t.status = 'EXPIRED' THEN 1 ELSE 0 END) as expiredTickets, " +
                        "COALESCE(AVG(t.price), 0) as averagePrice, " +
                        "COALESCE(SUM(CASE WHEN t.status IN ('USED', 'ACTIVE') THEN t.price ELSE 0 END), 0) as totalRevenue "
                        +
                        "FROM tickets t " +
                        "WHERE YEAR(t.created_at) = :year " +
                        "GROUP BY MONTH(t.created_at) " +
                        "ORDER BY MONTH(t.created_at)", nativeQuery = true)
        List<Object[]> findMonthlyTicketStatistics(@Param("year") Integer year);
}
