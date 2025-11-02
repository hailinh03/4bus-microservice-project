package com.mss.project.trip_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripCreatedEvent {
    private int tripId;
    private int busId;
    private int driverId;
    private String status;
}