package com.mss.project.trip_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripCreateRequest {
    String name;
    int routeId;
    int busId;
    List<Integer> driverIds;
    boolean isHoliday;
    LocalDateTime startTime;
}
