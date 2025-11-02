package com.mss.project.bus_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusImageDTO {
    private Integer id;
    @NotEmpty(message = "URL hình ảnh là bắt buộc")
    private String imageUrl;
    @NotEmpty(message = "publicId là bắt buộc")
    private String publicId;

}
