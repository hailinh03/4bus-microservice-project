package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.admin.ticket.AdminTicketListRequest;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketResponse;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketUpdateRequest;
import com.mss.project.booking_service.payload.admin.ticket.MonthlyTicketStatisticsRequest;
import com.mss.project.booking_service.payload.admin.ticket.MonthlyTicketStatisticsResponse;
import org.springframework.data.domain.Page;

public interface AdminTicketService {
    Page<AdminTicketResponse> getAllTickets(AdminTicketListRequest request);

    AdminTicketResponse getTicketById(Integer id);

    AdminTicketResponse updateTicket(Integer id, AdminTicketUpdateRequest request);

    void deleteTicket(Integer id);

    long countTicketsByStatus();

    MonthlyTicketStatisticsResponse getMonthlyTicketStatistics(MonthlyTicketStatisticsRequest request);
}
