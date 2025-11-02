package com.mss.project.booking_service.payload.admin.ticket;

import com.mss.project.booking_service.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicketListRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    private TicketStatus status;
    private Integer bookingId;
    private Integer tripId;
    private String seatCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
