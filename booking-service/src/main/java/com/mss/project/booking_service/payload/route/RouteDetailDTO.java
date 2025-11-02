package com.mss.project.booking_service.payload.route;

import com.mss.project.booking_service.payload.routepoint.RoutePointDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteDetailDTO {
    RoutePointDTO routePoint;
    int orderIndex;
    int durationFromPreviousPoint;
}
