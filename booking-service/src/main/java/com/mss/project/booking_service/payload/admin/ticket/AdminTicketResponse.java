package com.mss.project.booking_service.payload.admin.ticket;

import com.mss.project.booking_service.enums.TicketStatus;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicketResponse {
    private Integer id;
    private TicketStatus status;
    private Integer price;
    private String seatCode;
    private String seatId;
    private Integer bookingId;
    private String adminNote;
    private Integer tripId;
    private Instant createdAt;
    private Instant updatedAt;

    // Additional admin fields
    private AdminBookingResponse booking;
}
