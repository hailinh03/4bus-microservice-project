package com.mss.project.booking_service.payload.admin.ticket;

import com.mss.project.booking_service.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicketUpdateRequest {
    @NotNull(message = "Ticket status is required")
    private TicketStatus status;
    
    private Integer price;
    private String adminNote;
}
