package com.mss.project.booking_service.mapper;

import com.mss.project.booking_service.payload.ticket.TicketRequest;
import com.mss.project.booking_service.payload.ticket.TicketResponse;
import com.mss.project.booking_service.entities.Ticket;

public class TicketMapper {
    public static Ticket toEntity(TicketRequest request) {
        if (request == null)
            return null;
        Ticket ticket = new Ticket();
        ticket.setStatus(request.getStatus() != null ? request.getStatus() : null);
        ticket.setPrice(request.getPrice());
        ticket.setSeatCode(request.getSeatCode());
        ticket.setSeatId(request.getSeatId());
        ticket.setTripId(request.getTripId());
        return ticket;
    }

    public static TicketResponse toResponse(Ticket ticket) {
        if (ticket == null)
            return null;
        return TicketResponse.builder()
                .id(ticket.getId())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .price(ticket.getPrice())
                .seatCode(ticket.getSeatCode())
                .seatId(ticket.getSeatId())
                .bookingId(ticket.getBooking() != null ? ticket.getBooking().getId() : null)
                .tripId(ticket.getTripId())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
