package com.mss.project.bus_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusListResponse {
    List<BusResponse> buses;
    int totalPages;
    long totalElements;
    int currentPage;
    int pageSize;
}
