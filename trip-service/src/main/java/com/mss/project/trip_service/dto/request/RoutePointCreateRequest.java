package com.mss.project.trip_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoutePointCreateRequest {
    String name;
    String description;
    double latitude;
    double longitude;
    String fullAddress;
    int provinceId;
    @JsonProperty("isActive")
    boolean isActive;
}