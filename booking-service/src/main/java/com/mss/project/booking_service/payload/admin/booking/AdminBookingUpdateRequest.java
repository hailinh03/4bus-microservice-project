package com.mss.project.booking_service.payload.admin.booking;

import com.mss.project.booking_service.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingUpdateRequest {
    @NotNull(message = "Booking status is required")
    private BookingStatus status;
    
    private String adminNote;
}
