package com.mss.project.bus_service.service.impl;

import com.mss.project.bus_service.dto.request.BusCategoryRequest;
import com.mss.project.bus_service.dto.response.BusCategoryListResponse;
import com.mss.project.bus_service.dto.response.BusCategoryResponse;
import com.mss.project.bus_service.entity.Bus;
import com.mss.project.bus_service.entity.BusCategory;
import com.mss.project.bus_service.mapper.BusCategoryMapper;
import com.mss.project.bus_service.repository.BusCategoryRepository;
import com.mss.project.bus_service.service.IBusCategoryService;
import com.mss.project.bus_service.specification.BusCategorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusCategoryService implements IBusCategoryService {

    private final BusCategoryRepository busCategoryRepository;

    @Override
    public BusCategoryResponse create(BusCategoryRequest request) {

        String categoryName = request.getName();
        // check categoru name
        log.info("Creating bus category with name: {}", categoryName);
        if (!busCategoryRepository.existsByName(categoryName)) {
            BusCategory busCategory = new BusCategory();
            busCategory.setName(categoryName);
            busCategory.setTotalSeats(request.getTotalSeats());
            busCategory.setDescription(request.getDescription());
             busCategory.setSeatCodes(request.getSeatCodes());
            busCategory.setMaxPrice(request.getMaxPrice());
            busCategory.setMinPrice(request.getMinPrice());
            busCategoryRepository.save(busCategory);
            return new BusCategoryMapper().toResponse(busCategory);

        } else {
            throw new RuntimeException("Bus Category với tên " + categoryName + " đã tồn tại.");
        }

    }

    @Override
    public BusCategoryListResponse getAllBusCategories(int page, int size, String sortBy, String sortDirection, String searchString) {
        Specification<BusCategory> specification = Specification.where(
                BusCategorySpecification.hasSearchString(searchString));


        Pageable pageable =  PageRequest.of(page, size, sortDirection.equalsIgnoreCase("asc") ?
                org.springframework.data.domain.Sort.by(sortBy).ascending() :
                org.springframework.data.domain.Sort.by(sortBy).descending());

        Page<BusCategory> busCategoryPage = busCategoryRepository.findAll(specification,pageable);
        return new BusCategoryMapper().toResponseList(
                busCategoryPage.getContent(),
                busCategoryPage.getTotalPages(),
                busCategoryPage.getTotalElements(),
                busCategoryPage.getNumber(),
                busCategoryPage.getSize()
        );
    }

    @Override
    public BusCategoryResponse getBusCategoryById(int id) {
        BusCategory busCategory = busCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bus Category với id là:  " + id));
        return new BusCategoryMapper().toResponse(busCategory);
    }

    @Override
    public BusCategoryResponse deleteCategory(int id) {
        BusCategory busCategory = busCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bus Category với id là:  " + id));
        if(busCategory.getBuses().isEmpty()) {
            busCategory.setDeleted(true);
            busCategoryRepository.save(busCategory);
            return new BusCategoryMapper().toResponse(busCategory);
        } else {
            throw new RuntimeException("Không thể xóa Bus Category với id " + id + " vì nó đang được sử dụng bởi các xe buýt.");
        }
    }

    @Override
    public BusCategoryResponse updateCategory(int id, BusCategoryRequest request) {
        BusCategory busCategory = busCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bus Category với id là: " + id));
        if(!busCategory.getName().equals(request.getName())){
            BusCategory existingBusCategory = busCategoryRepository.findBusCategoriesByName(request.getName());
            if(existingBusCategory != null){
                throw new RuntimeException("Tên loại xe đã tồn tại: " + existingBusCategory.getName());
            }
        }
                 busCategory.setName(request.getName());
                 busCategory.setTotalSeats(request.getTotalSeats());
                 busCategory.setDescription(request.getDescription());
                 busCategory.setSeatCodes(request.getSeatCodes());
                 busCategory.setMaxPrice(request.getMaxPrice());
                 busCategory.setMinPrice(request.getMinPrice());
                 busCategoryRepository.save(busCategory);
        return new BusCategoryMapper().toResponse(busCategory);
    }

    @Override
    public BusCategoryListResponse searchBusCategories(String searchTerm, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<BusCategory> busCategoryPage = busCategoryRepository.searchBusCategories(searchTerm, pageable);
        return new BusCategoryMapper().toResponseList(
                busCategoryPage.getContent(),
                busCategoryPage.getTotalPages(),
                busCategoryPage.getTotalElements(),
                busCategoryPage.getNumber(),
                busCategoryPage.getSize()
        );
    }

}
