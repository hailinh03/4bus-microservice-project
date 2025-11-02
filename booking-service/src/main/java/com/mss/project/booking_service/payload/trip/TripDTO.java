package com.mss.project.booking_service.payload.trip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mss.project.booking_service.payload.bus.BusDTO;
import com.mss.project.booking_service.payload.route.RouteDTO;
import com.mss.project.booking_service.payload.route.RouteDetailDTO;
import com.mss.project.booking_service.payload.user.UserDTO;
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
public class TripDTO {
    private int id;
    private String name;
    private String status;
    private String origin;
    private String destination;
    private int estimateDuration;
    private boolean isHoliday;
    private BusDTO bus;
    private List<UserDTO> drivers;
    private RouteDTO route;
    private RouteDetailDTO from;
    private RouteDetailDTO to;
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime updatedAt;
}
