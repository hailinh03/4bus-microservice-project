package com.mss.project.booking_service.payload.ticket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Integer id;
    private String status;
    private Integer price;
    private String seatCode;
    private String seatId;
    private Integer bookingId;
    private Integer tripId;
    private Instant createdAt;
    private Instant updatedAt;
}
