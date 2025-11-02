package com.mss.project.booking_service.payload.booking;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BookingRequest {
    private Integer tripId;
    private List<Seat> seats;
}
