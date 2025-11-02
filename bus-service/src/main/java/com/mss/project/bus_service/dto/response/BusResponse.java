package com.mss.project.bus_service.dto.response;

import com.mss.project.bus_service.enums.BusStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BusResponse {
    private int id;
    private String name;
    private String plateNumber;
    private String color;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BusStatus status;
    private boolean hasDeleted;
    private BusCategoryResponse category;
    private List<BusImageDTOResponse> images;

}
