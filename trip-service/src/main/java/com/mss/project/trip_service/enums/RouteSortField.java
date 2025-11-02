package com.mss.project.trip_service.enums;

import lombok.Getter;

@Getter
public enum RouteSortField {
    NAME("name"),
    ORIGIN("origin"),
    DESTINATION("destination"),
    DESCRIPTION("description"),
    DURATION("duration"),
    DISTANCE("distance"),
    TOTAL_ROUTE_POINTS("totalRoutePoints"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String fieldName;
    RouteSortField(String fieldName) {
        this.fieldName = fieldName;
    }

}
