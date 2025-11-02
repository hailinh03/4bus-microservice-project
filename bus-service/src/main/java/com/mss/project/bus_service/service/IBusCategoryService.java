package com.mss.project.bus_service.service;

import com.mss.project.bus_service.dto.request.BusCategoryRequest;
import com.mss.project.bus_service.dto.response.BusCategoryListResponse;
import com.mss.project.bus_service.dto.response.BusCategoryResponse;

import java.util.List;

public interface IBusCategoryService {
    BusCategoryResponse create(BusCategoryRequest request);
    BusCategoryListResponse getAllBusCategories(int page, int size, String sortBy, String sortDirection, String searchString);
    BusCategoryResponse getBusCategoryById(int id);
    BusCategoryResponse deleteCategory(int id);
    BusCategoryResponse updateCategory(int id, BusCategoryRequest request);
    BusCategoryListResponse searchBusCategories(String searchTerm, int page, int size);
}
