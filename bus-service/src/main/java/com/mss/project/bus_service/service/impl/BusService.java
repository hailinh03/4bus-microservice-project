package com.mss.project.bus_service.service.impl;

import com.mss.project.bus_service.dto.request.BusDTO;
import com.mss.project.bus_service.dto.request.BusImageDTO;
import com.mss.project.bus_service.dto.response.BusResponse;
import com.mss.project.bus_service.dto.response.BusListResponse;
import com.mss.project.bus_service.entity.Bus;
import com.mss.project.bus_service.entity.BusCategory;
import com.mss.project.bus_service.entity.BusImage;
import com.mss.project.bus_service.enums.BusStatus;
import com.mss.project.bus_service.mapper.BusMapper;
import com.mss.project.bus_service.repository.BusCategoryRepository;
import com.mss.project.bus_service.repository.BusImageRepository;
import com.mss.project.bus_service.repository.BusRepository;
import com.mss.project.bus_service.service.IBusService;
import com.mss.project.bus_service.specification.BusSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService implements IBusService {
    private final BusRepository busRepository;
    private final BusCategoryRepository busCategoryRepository;
    private final BusMapper busMapper;
    private final BusImageRepository busImageRepository;
    @Override
    public BusResponse createBus(BusDTO busDTO) {
        validateDuplicatePublicIds(busDTO.getImages());
        checkPublicIdExistsInDatabase(busDTO.getImages());
        checkNameBusExists(busDTO.getName());
        checkPlateNumberExists(busDTO.getPlateNumber());
        Bus bus = new Bus();
        bus.setName(busDTO.getName());
        bus.setDescription(busDTO.getDescription());
        bus.setColor(busDTO.getColor());
        bus.setPlateNumber(busDTO.getPlateNumber());
        BusCategory category = busCategoryRepository
                .findById(busDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại Bus với ID: " + busDTO.getCategoryId()));
        bus.setCategory(category);

        List<BusImageDTO> images = busDTO.getImages();

        if (images != null && !images.isEmpty()) {
            List<BusImage> busImages = images.stream().map(busMapper::mapToBusImage)
                    .peek(busImage -> busImage.setBus(bus))
                    .toList();
            bus.setImages(busImages);
        }else{
            bus.setImages(null);
        }
        Bus savedBus = busRepository.save(bus);
        return busMapper.mapToBusCreateResponse(savedBus);
    }

    @Override
    public BusListResponse getAllBuses(int page, int size, String sortBy, String sortDirection, String searchString) {
        Specification<Bus> specification = Specification.where(BusSpecification.hasSearchString(searchString));
//        if ("category.name".equals(sortBy)) {
//            specification = specification.and(BusSpecification.withCategoryJoin());
//        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Bus> busPage = busRepository.findAll(specification,pageable);
        List<BusResponse> busResponses = busPage.getContent().stream()
                .map(busMapper::mapToBusCreateResponse)
                .toList();
          return new BusListResponse(
                busResponses,
                busPage.getTotalPages(),
                busPage.getTotalElements(),
                busPage.getNumber(),
                busPage.getSize()
        );
    }

    @Override
    public BusResponse getBusById(int id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Bus với ID: " + id));
        return busMapper.mapToBusCreateResponse(bus);
    }

    @Override
    @Transactional
    public BusResponse updateBus(int id, BusDTO busDTO) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Bus với ID: " + id));

        if (!bus.getName().equals(busDTO.getName())) {
            checkNameBusExists(busDTO.getName());
        }
        if (!bus.getPlateNumber().equals(busDTO.getPlateNumber())) {
            checkPlateNumberExists(busDTO.getPlateNumber());
        }
        if(bus.getStatus() != null && bus.getStatus() != BusStatus.AVAILABLE) {
            throw new IllegalArgumentException("Không thể cập nhật Bus với ID " + id + " vì nó không có trạng thái AVAILABLE.");
        }
        bus.setName(busDTO.getName());
        bus.setDescription(busDTO.getDescription());
        bus.setColor(busDTO.getColor());
        bus.setPlateNumber(busDTO.getPlateNumber());
        BusCategory category = bus.getCategory();

       if(!(busDTO.getCategoryId() ==(category.getId()))) {
            category = busCategoryRepository
                    .findById(busDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại Bus với ID: " + busDTO.getCategoryId()));
            bus.setCategory(category);
        }
        // Xử lý danh sách hình ảnh
        // Lấy danh sách hình ảnh từ yêu cầu cập nhật
        List<BusImageDTO> images = busDTO.getImages();
        if (images != null && !images.isEmpty()) {
            List<BusImage> currentImages = bus.getImages() != null ? bus.getImages() : new ArrayList<>();
            // lấy danh sách hình ảnh đã tồn tại trong cơ sở dữ liệu
            List<BusImageDTO> existingImages = images.stream()
                    .filter(img -> img.getId() != null && img.getId() > 0)
                    .toList();
            // Lấy danh sách hình ảnh mới (không có ID hoặc ID <= 0)
            List<BusImageDTO> newImages = images.stream()
                    .filter(img -> img.getId() == null || img.getId() <= 0)
                    .toList();
            // kiểm tra xem hình ảnh mới có hợp le ko
            if (!newImages.isEmpty()) {
                validateDuplicatePublicIds(newImages);
                checkPublicIdExistsInDatabase(newImages);
            }
            // lưu ảnh đã tồn tại vào Set
            Set<Integer> keepImageIds = existingImages.stream()
                    .map(BusImageDTO::getId)
                    .collect(Collectors.toSet());
            List<BusImage> imagesToRemove = currentImages.stream()
                    .filter(img -> !keepImageIds.contains(img.getId()))
                    .toList();
            currentImages.removeAll(imagesToRemove);
            List<BusImage> busImagesToAdd = newImages.stream()
                    .map(busMapper::mapToBusImage)
                    .peek(busImage -> busImage.setBus(bus))
                    .toList();
            currentImages.addAll(busImagesToAdd);

            bus.setImages(currentImages);
        }
        Bus updatedBus = busRepository.save(bus);
        return busMapper.mapToBusCreateResponse(updatedBus);
    }

    @Override
    public BusResponse deleteBus(int id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Bus với ID: " + id));
        if(bus.getStatus() != null && bus.getStatus() != BusStatus.AVAILABLE) {
            throw new IllegalArgumentException("Không thể xóa Bus với ID " + id + " vì nó không có trạng thái AVAILABLE.");
        }
        bus.setDeleted(true);
        if(bus.getImages() != null) {
            for (BusImage image : bus.getImages()) {
                image.setDeleted(true);
            }
        }
        Bus deletedBus = busRepository.save(bus);
        BusResponse response = busMapper.mapToBusCreateResponse(deletedBus);
        response.setHasDeleted(true);
        return response;
    }

    private void validateDuplicatePublicIds(List<BusImageDTO> images) {
        Set<String> seen = new HashSet<>();
        for (BusImageDTO image : images) {
            if (!seen.add(image.getPublicId())) {
                throw new IllegalArgumentException("PublicId bị trùng trong danh sách gửi tạo: " + image.getPublicId());
            }
        }
    }
    private void checkPublicIdExistsInDatabase(List<BusImageDTO> images) {
        for (BusImageDTO image : images) {
            if (busImageRepository.existsByPublicId(image.getPublicId())) {
                throw new IllegalArgumentException("PublicId đã tồn tại: " + image.getPublicId());
            }
        }
    }
    private void checkNameBusExists(String name) {
        if (busRepository.existsByName(name)) {
            throw new IllegalArgumentException("Bus với tên này đã tồn tại: " + name);
        }    }
    private void checkPlateNumberExists(String plateNumber) {
        if (busRepository.existsByPlateNumber(plateNumber)) {
            throw new IllegalArgumentException("Biển số xe này đã tồn tại: " + plateNumber);
        }
    }

    @Override
    public BusListResponse searchBuses(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bus> busPage = busRepository.searchBuses(query, pageable);
        
        List<BusResponse> busResponses = busPage.getContent().stream()
                .map(busMapper::mapToBusCreateResponse)
                .toList();
                
        return new BusListResponse(
                busResponses,
                busPage.getTotalPages(),
                busPage.getTotalElements(),
                busPage.getNumber(),
                busPage.getSize()
        );
    }

}
