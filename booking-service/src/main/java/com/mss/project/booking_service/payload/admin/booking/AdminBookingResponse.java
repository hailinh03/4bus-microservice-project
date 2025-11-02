package com.mss.project.booking_service.payload.admin.booking;

import com.mss.project.booking_service.enums.BookingStatus;
import com.mss.project.booking_service.payload.user.UserDTO;
import com.mss.project.booking_service.payload.trip.TripDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingResponse {
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

    // Additional admin fields
    private UserDTO user;
    private TripDTO trip;
}
