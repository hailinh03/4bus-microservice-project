package com.mss.project.booking_service.service.impl;

import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.exception.BookingException;
import com.mss.project.booking_service.mapper.AdminTicketMapper;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketListRequest;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketResponse;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketUpdateRequest;
import com.mss.project.booking_service.payload.admin.ticket.MonthlyTicketStatisticsRequest;
import com.mss.project.booking_service.payload.admin.ticket.MonthlyTicketStatisticsResponse;
import com.mss.project.booking_service.repository.TicketRepository;
import com.mss.project.booking_service.service.AdminTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mss.project.booking_service.mapper.AdminTicketMapper.mapToAdminResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTicketServiceImpl implements AdminTicketService {

    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminTicketResponse> getAllTickets(AdminTicketListRequest request) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Instant startDate = request.getStartDate() != null
                    ? request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;
            Instant endDate = request.getEndDate() != null
                    ? request.getEndDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;

            Page<Ticket> tickets = ticketRepository.findAllWithFilters(
                    request.getStatus(),
                    request.getBookingId(),
                    request.getTripId(),
                    request.getSeatCode(),
                    startDate,
                    endDate,
                    pageable);

            return tickets.map(AdminTicketMapper::mapToAdminResponse);

        } catch (Exception e) {
            log.error("Error fetching tickets: {}", e.getMessage(), e);
            throw new BookingException("Failed to fetch tickets: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTicketResponse getTicketById(Integer id) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new BookingException("Ticket not found with id: " + id));

            return mapToAdminResponse(ticket);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching ticket by id {}: {}", id, e.getMessage(), e);
            throw new BookingException("Failed to fetch ticket: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AdminTicketResponse updateTicket(Integer id, AdminTicketUpdateRequest request) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new BookingException("Ticket not found with id: " + id));

            ticket.setStatus(request.getStatus());
            if (request.getPrice() != null) {
                ticket.setPrice(request.getPrice());
            }
            if (request.getAdminNote() != null) {
                ticket.setAdminNote(request.getAdminNote());
            }

            Ticket updatedTicket = ticketRepository.save(ticket);

            log.info("Admin updated ticket {} to status {}", id, request.getStatus());
            return mapToAdminResponse(updatedTicket);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating ticket {}: {}", id, e.getMessage(), e);
            throw new BookingException("Failed to update ticket: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteTicket(Integer id) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new BookingException("Ticket not found with id: " + id));

            ticketRepository.delete(ticket);

            log.info("Admin deleted ticket {}", id);

        } catch (BookingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting ticket {}: {}", id, e.getMessage(), e);
            throw new BookingException("Failed to delete ticket: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countTicketsByStatus() {
        try {
            return ticketRepository.count();
        } catch (Exception e) {
            log.error("Error counting tickets: {}", e.getMessage(), e);
            throw new BookingException("Failed to count tickets: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyTicketStatisticsResponse getMonthlyTicketStatistics(MonthlyTicketStatisticsRequest request) {
        try {
            Integer year = request.getYear();
            log.info("Fetching monthly ticket statistics for year: {}", year);

            // Get monthly statistics from database
            List<Object[]> rawData = ticketRepository.findMonthlyTicketStatistics(year);

            // Convert raw data to Map for easier processing
            Map<Integer, MonthlyTicketStatisticsResponse.MonthlyTicketData> dataMap = rawData.stream()
                    .collect(Collectors.toMap(
                            row -> ((Number) row[0]).intValue(), // month number
                            row -> MonthlyTicketStatisticsResponse.MonthlyTicketData.builder()
                                    .month(Month.of(((Number) row[0]).intValue()))
                                    .monthName(Month.of(((Number) row[0]).intValue()).name())
                                    .monthNumber(((Number) row[0]).intValue())
                                    .totalTickets(((Number) row[1]).longValue())
                                    .activeTickets(((Number) row[2]).longValue())
                                    .usedTickets(((Number) row[3]).longValue())
                                    .cancelledTickets(((Number) row[4]).longValue())
                                    .expiredTickets(((Number) row[5]).longValue())
                                    .averageTicketPrice(row[6] != null ? ((Number) row[6]).doubleValue() : 0.0)
                                    .totalRevenue(row[7] != null ? ((Number) row[7]).longValue() : 0L)
                                    .build()));

            // Fill in missing months with zero values (January to December)
            List<MonthlyTicketStatisticsResponse.MonthlyTicketData> monthlyData = new ArrayList<>();
            for (int monthNum = 1; monthNum <= 12; monthNum++) {
                MonthlyTicketStatisticsResponse.MonthlyTicketData monthData = dataMap.getOrDefault(monthNum,
                        MonthlyTicketStatisticsResponse.MonthlyTicketData.builder()
                                .month(Month.of(monthNum))
                                .monthName(Month.of(monthNum).name())
                                .monthNumber(monthNum)
                                .totalTickets(0L)
                                .activeTickets(0L)
                                .usedTickets(0L)
                                .cancelledTickets(0L)
                                .expiredTickets(0L)
                                .averageTicketPrice(0.0)
                                .totalRevenue(0L)
                                .build());
                monthlyData.add(monthData);
            }

            // Calculate totals for the year
            Long totalTickets = monthlyData.stream()
                    .mapToLong(MonthlyTicketStatisticsResponse.MonthlyTicketData::getTotalTickets).sum();
            Long totalActiveTickets = monthlyData.stream()
                    .mapToLong(MonthlyTicketStatisticsResponse.MonthlyTicketData::getActiveTickets).sum();
            Long totalUsedTickets = monthlyData.stream()
                    .mapToLong(MonthlyTicketStatisticsResponse.MonthlyTicketData::getUsedTickets).sum();
            Long totalCancelledTickets = monthlyData.stream()
                    .mapToLong(MonthlyTicketStatisticsResponse.MonthlyTicketData::getCancelledTickets).sum();
            Long totalExpiredTickets = monthlyData.stream()
                    .mapToLong(MonthlyTicketStatisticsResponse.MonthlyTicketData::getExpiredTickets).sum();

            return MonthlyTicketStatisticsResponse.builder()
                    .year(year)
                    .totalTickets(totalTickets)
                    .totalActiveTickets(totalActiveTickets)
                    .totalUsedTickets(totalUsedTickets)
                    .totalCancelledTickets(totalCancelledTickets)
                    .totalExpiredTickets(totalExpiredTickets)
                    .monthlyData(monthlyData)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching monthly ticket statistics for year {}: {}", request.getYear(), e.getMessage(), e);
            throw new BookingException("Failed to fetch monthly ticket statistics: " + e.getMessage());
        }
    }

}
