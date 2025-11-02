package com.mss.project.booking_service.payload.ticket;

import com.mss.project.booking_service.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelTicketResponse {
    private Integer id;
    private TicketStatus status;
    private Integer price;
    private String seatCode;
    private String seatId;
    private Integer bookingId;
    private Integer tripId;
    private String cancellationReason;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
