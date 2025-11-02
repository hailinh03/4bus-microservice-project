package com.mss.project.trip_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripListDTO {
    private List<TripDTO> trips;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}
