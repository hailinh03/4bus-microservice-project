package com.mss.project.booking_service.payload.bus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusImageDTO {
    private String imageUrl;
    private String publicId;
}