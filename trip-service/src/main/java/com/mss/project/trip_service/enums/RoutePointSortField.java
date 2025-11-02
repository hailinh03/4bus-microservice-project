package com.mss.project.trip_service.enums;

import lombok.Getter;

@Getter
public enum RoutePointSortField {
    CREATED_AT("createdAt"),
    NAME("name"),
    PROVINCE("province"),
    DISTRICT("district"),
    UPDATED_AT("updatedAt");

    private final String field;
    RoutePointSortField(String field) {
        this.field = field;
    }
}
