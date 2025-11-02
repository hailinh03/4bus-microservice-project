package com.mss.project.trip_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mss.project.trip_service.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripDTO {
    private int id;
    private String name;
    private String status;
    private String origin;
    private String destination;
    private boolean isHoliday;
    private int estimateDuration;
    private BusDTO bus;
    private List<UserDTO> drivers;
    private RouteDTO route;
    private RouteDetailDTO from;
    private RouteDetailDTO to;
    private List<String> bookedSeats;
    private LocalDateTime startTime;
    private LocalDateTime estimateEndTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime updatedAt;
}
