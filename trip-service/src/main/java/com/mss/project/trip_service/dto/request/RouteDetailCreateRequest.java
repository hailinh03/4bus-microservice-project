package com.mss.project.trip_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteDetailCreateRequest {
    int routePointId;
    int orderIndex;
    int durationFromPreviousPoint;
}
