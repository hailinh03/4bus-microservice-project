package com.mss.project.trip_service.enums;

import lombok.Getter;

@Getter
public enum TripStatus {
    PLANNED("Planned"),
    COMPLETED("Completed"),
    STARTED("Started"),
    CANCELLED("Cancelled");

    private final String status;

    TripStatus(String status) {
        this.status = status;
    }

    public static TripStatus fromString(String status) {
        for (TripStatus tripStatus : TripStatus.values()) {
            if (tripStatus.getStatus().equalsIgnoreCase(status)) {
                return tripStatus;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }


}
