package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.ticket.CancelTicketRequest;
import com.mss.project.booking_service.payload.ticket.CancelTicketResponse;
import com.mss.project.booking_service.payload.ticket.TicketRequest;
import com.mss.project.booking_service.payload.ticket.TicketResponse;
import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketRequest ticketRequest);

    TicketResponse getTicketById(Integer id);

    List<TicketResponse> getAllTickets();

    TicketResponse updateTicket(Integer id, TicketRequest ticketRequest);

    void deleteTicket(Integer id);

    List<String> getBookedSeatsByTripId(Integer tripId);

    CancelTicketResponse cancelTicket(Integer ticketId, CancelTicketRequest request);

    void markTicketsUsedByTripId(Integer tripId);

    void markTicketsExpiredByTripId(Integer tripId);
}
