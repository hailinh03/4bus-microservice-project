package com.mss.project.booking_service.service.impl;

import com.mss.project.booking_service.entities.Booking;
import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.enums.TicketStatus;
import com.mss.project.booking_service.exception.BookingException;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingListRequest;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingResponse;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingUpdateRequest;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.repository.TicketRepository;
import com.mss.project.booking_service.service.AdminBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBookingServiceImpl implements AdminBookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminBookingResponse> getAllBookings(AdminBookingListRequest request) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Instant startDate = request.getStartDate() != null
                    ? request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;
            Instant endDate = request.getEndDate() != null
                    ? request.getEndDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;

            Page<Booking> bookings = bookingRepository.findAllWithFilters(
                    request.getStatus(),
                    request.getUserId(),
                    request.getTripId(),
                    startDate,
                    endDate,
                    pageable);

            return bookings.map(this::mapToAdminResponse);

        } catch (Exception e) {
            log.error("Error fetching bookings: {}", e.getMessage(), e);
            throw new BookingException("Failed to fetch bookings: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminBookingResponse getBookingById(Integer id) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new BookingException("Booking not found with id: " + id));

            return mapToAdminResponse(booking);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching booking by id {}: {}", id, e.getMessage(), e);
            throw new BookingException("Failed to fetch booking: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AdminBookingResponse updateBooking(Integer id, AdminBookingUpdateRequest request) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new BookingException("Booking not found with id: " + id));

            // update ticket status if booking is cancelled
            if (request.getStatus() == BookingStatus.CANCELLED) {
                List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
                for (Ticket ticket : tickets) {
                    ticket.setStatus(TicketStatus.CANCELLED);
                    ticketRepository.save(ticket);
                }
            }

            booking.setStatus(request.getStatus());

            Booking updatedBooking = bookingRepository.save(booking);

            log.info("Admin updated booking {} to status {}", id, request.getStatus());
            return mapToAdminResponse(updatedBooking);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating booking {}: {}", id, e.getMessage(), e);
            throw new BookingException("Failed to update booking: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteBooking(Integer id) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new BookingException("Booking not found with id: " + id));

            bookingRepository.delete(booking);

            log.info("Admin deleted booking {}", id);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting booking {}: {}", id, e.getMessage(), e);
            throw new BookingException("Failed to delete booking: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countBookingsByStatus() {
        try {
            // This would typically return a more detailed count by status
            return bookingRepository.count();
        } catch (Exception e) {
            log.error("Error counting bookings: {}", e.getMessage(), e);
            throw new BookingException("Failed to count bookings: " + e.getMessage());
        }
    }

    private AdminBookingResponse mapToAdminResponse(Booking booking) {
        return AdminBookingResponse.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .numberOfTickets(booking.getNumberOfTickets())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .seatCodes(booking.getSeatCodes())
                .orderCode(booking.getOrderCode())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
