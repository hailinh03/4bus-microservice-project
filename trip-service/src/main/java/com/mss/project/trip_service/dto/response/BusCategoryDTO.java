package com.mss.project.trip_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BusCategoryDTO {
    private int id;
    private String name;
    private int totalSeats;
    private String description;
    private String seatCodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
