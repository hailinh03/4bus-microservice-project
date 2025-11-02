package com.mss.project.bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BusCategoryListResponse {
    List<BusCategoryResponse> busCategories;
    int totalPages;
    long totalElements;
    int currentPage;
    int pageSize;
}
