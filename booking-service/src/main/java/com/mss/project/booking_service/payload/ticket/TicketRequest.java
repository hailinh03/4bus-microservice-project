package com.mss.project.booking_service.payload.ticket;

import com.mss.project.booking_service.enums.TicketStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {
    private TicketStatus status;
    private Integer price;
    private String seatCode;
    private String seatId;
    private Integer bookingId;
    private Integer tripId;
}

