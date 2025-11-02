package com.mss.project.bus_service.mapper;

import com.mss.project.bus_service.dto.response.BusCategoryListResponse;
import com.mss.project.bus_service.dto.response.BusCategoryResponse;
import com.mss.project.bus_service.entity.BusCategory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusCategoryMapper {

    public BusCategoryResponse toResponse(BusCategory busCategory) {
        if (busCategory == null) {
            return null;
        }
        BusCategoryResponse response = new BusCategoryResponse();
        response.setId(busCategory.getId());
        response.setName(busCategory.getName());
        response.setTotalSeats(busCategory.getTotalSeats());
        response.setDescription(busCategory.getDescription());
        response.setSeatCodes(busCategory.getSeatCodes());
        response.setMaxPrice(busCategory.getMaxPrice());
        response.setMinPrice(busCategory.getMinPrice());
        response.setCreatedAt(busCategory.getCreatedAt());
        response.setUpdatedAt(busCategory.getUpdatedAt());
        response.setDeleted(busCategory.isDeleted());

        return response;
    }
    public BusCategoryListResponse toResponseList(List<BusCategory> busCategories,
                                                  int totalPages, long totalElements, int currentPage, int pageSize) {
        BusCategoryListResponse response = new BusCategoryListResponse();
        response.setBusCategories(busCategories.stream().map(this::toResponse).toList());
        response.setTotalPages(totalPages);
        response.setTotalElements(totalElements);
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        return response;

    }
}
