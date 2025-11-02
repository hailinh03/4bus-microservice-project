package com.mss.project.trip_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteCreateRequest {
    String name;
    String origin;
    String destination;
    String description;
    int distance;
    int duration;
    int totalRoutePoints;
    List<RouteDetailCreateRequest> routePoints;
}
