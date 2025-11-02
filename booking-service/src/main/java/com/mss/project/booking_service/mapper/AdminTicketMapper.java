package com.mss.project.booking_service.mapper;

import com.mss.project.booking_service.entities.Ticket;
import com.mss.project.booking_service.payload.admin.ticket.AdminTicketResponse;

public class AdminTicketMapper {
    public static AdminTicketResponse mapToAdminResponse(Ticket ticket) {
        return AdminTicketResponse.builder()
                .id(ticket.getId())
                .status(ticket.getStatus())
                .price(ticket.getPrice())
                .seatCode(ticket.getSeatCode())
                .seatId(ticket.getSeatId())
                .adminNote(ticket.getAdminNote())
                .bookingId(ticket.getBooking().getId())
                .tripId(ticket.getTripId())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
