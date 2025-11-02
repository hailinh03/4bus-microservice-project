package com.mss.project.bus_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BusDTO {
    private String name;
    private String plateNumber;
    private String color;
    private String description;
    private int categoryId;
    @NotEmpty(message = "Danh sách hình ảnh là bắt buộc")
    private List<@Valid BusImageDTO> images;
}
