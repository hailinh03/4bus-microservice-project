package com.mss.project.trip_service.dto.response;

import com.mss.project.trip_service.enums.BusStatus;
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
public class BusDTO {
    private int id;
    private String name;
    private String plateNumber;
    private String color;
    private String description;
    private boolean hasDeleted;
    private BusCategoryDTO category;
    private BusStatus status;
    private List<BusImageDTO> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
