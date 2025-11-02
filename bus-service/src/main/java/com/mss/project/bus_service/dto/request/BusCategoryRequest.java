package com.mss.project.bus_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusCategoryRequest {

    @NotEmpty(message = "Tên loại xe là bắt buộc")
    private String name;
    private int totalSeats;
    private String description;
    private String seatCodes;
    private long maxPrice;
    private long minPrice;
}
