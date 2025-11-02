package com.mss.project.booking_service.payload.booking;

import lombok.Data;

@Data
public class Seat{
    private String seatId;
    private String seatCode;
    private Double price;
}
