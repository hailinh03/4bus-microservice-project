package com.mss.project.bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BusCategoryResponse {
    private int id;
    private String name;
    private int totalSeats;
    private String description;
    private String seatCodes;
    private long maxPrice;
    private long minPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
