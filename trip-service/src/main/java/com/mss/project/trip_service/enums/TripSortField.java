package com.mss.project.trip_service.enums;

import lombok.Getter;

@Getter
public enum TripSortField {
    START_TIME("start_time"),
    ESTIMATE_END_TIME("estimate_end_time"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    private final String fieldName;
    TripSortField(String fieldName) {
        this.fieldName = fieldName;
    }
}
