package com.mss.project.booking_service.payload.booking;

import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.payload.ticket.TicketResponse;
import com.mss.project.booking_service.payload.trip.TripDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryResponse {
    private Integer id;
    private BookingStatus status;
    private Double totalPrice;
    private Integer numberOfTickets;
    private Long userId;
    private Integer tripId;
    private String seatCodes;
    private Long orderCode;
    private Instant createdAt;
    private Instant updatedAt;

    // Additional fields for booking history
    private TripDTO trip;
    private List<TicketResponse> tickets;
}
