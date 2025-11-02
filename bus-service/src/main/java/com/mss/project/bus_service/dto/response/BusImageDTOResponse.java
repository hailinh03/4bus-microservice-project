package com.mss.project.bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusImageDTOResponse {
    private int id;
    private String imageUrl;
    private String publicId;
}
