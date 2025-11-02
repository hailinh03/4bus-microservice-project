package com.mss.project.trip_service.service.impl;

import com.mss.project.trip_service.dto.request.RoutePointCreateRequest;
import com.mss.project.trip_service.dto.response.RoutePointDTO;
import com.mss.project.trip_service.dto.response.RoutePointListDTO;
import com.mss.project.trip_service.entity.Province;
import com.mss.project.trip_service.entity.RouteDetail;
import com.mss.project.trip_service.entity.RoutePoint;
import com.mss.project.trip_service.enums.RoutePointSortField;
import com.mss.project.trip_service.mapper.RoutePointMapper;
import com.mss.project.trip_service.repository.ProvinceRepository;
import com.mss.project.trip_service.repository.RouteDetailRepository;
import com.mss.project.trip_service.repository.RoutePointRepository;
import com.mss.project.trip_service.service.IRoutePointService;
import com.mss.project.trip_service.specification.RoutePointSpecification;
import com.mss.project.trip_service.utils.Prechecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutePointService implements IRoutePointService {

    private final RoutePointRepository routePointRepository;
    private final ProvinceRepository provinceRepository;
    private final RouteDetailRepository routeDetailRepository;

    @Override
    public RoutePointDTO createRoutePoint(RoutePointCreateRequest routePointCreateRequest) {
        Province province = provinceRepository.findById(routePointCreateRequest.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found"));
        RoutePoint routePoint = RoutePoint.builder()
                .name(routePointCreateRequest.getName())
                .description(routePointCreateRequest.getDescription())
                .latitude(routePointCreateRequest.getLatitude())
                .longitude(routePointCreateRequest.getLongitude())
                .province(province)
                .isActive(routePointCreateRequest.isActive())
                .fullAddress(routePointCreateRequest.getFullAddress())
                .build();
        try {
            RoutePoint savedRoutePoint = routePointRepository.save(routePoint);
            return RoutePointMapper.toRoutePointDTO(savedRoutePoint);
        } catch (Exception e) {
            log.error("Error creating route point: {}", e.getMessage());
            throw new RuntimeException("Something went wrong while creating the route point");
        }
    }

    @Override
    public RoutePointListDTO getAllRoutePoints(String searchString, Integer page, Integer size, RoutePointSortField sortBy, Sort.Direction sortDir, boolean isActive) {
        Prechecker.checkPaginationParameters(page, size);
        Specification<RoutePoint> spec = RoutePointSpecification.hasSearchString(searchString)
                .and(RoutePointSpecification.hasActive(isActive))
                .and(RoutePointSpecification.hasDeleted(false));
        Pageable pageable = PageRequest.of(page, size, sortDir, sortBy.getField());
        Page<RoutePoint> routePointPage = routePointRepository.findAll(spec, pageable);
        return RoutePointMapper.toRoutePointListDTO(routePointPage);
    }

    @Override
    public RoutePointDTO getRoutePointById(int id) {
        RoutePoint routePoint = routePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route point not found with id: " + id));
        return RoutePointMapper.toRoutePointDTO(routePoint);
    }

    @Override
    public void updateRoutePoint(int id, RoutePointCreateRequest routePointCreateRequest) {
        RoutePoint existingRoutePoint = routePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route point not found"));
        Province province = provinceRepository.findById(routePointCreateRequest.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found with id: " + routePointCreateRequest.getProvinceId()));
        existingRoutePoint.setName(routePointCreateRequest.getName());
        existingRoutePoint.setDescription(routePointCreateRequest.getDescription());
        existingRoutePoint.setLatitude(routePointCreateRequest.getLatitude());
        existingRoutePoint.setLongitude(routePointCreateRequest.getLongitude());
        existingRoutePoint.setProvince(province);
        existingRoutePoint.setActive(routePointCreateRequest.isActive());
        existingRoutePoint.setFullAddress(routePointCreateRequest.getFullAddress());
        try {
            routePointRepository.save(existingRoutePoint);
        } catch (Exception e) {
            log.error("Error updating route point: {}", e.getMessage());
            throw new RuntimeException("Something went wrong while updating the route point");
        }
    }

    @Override
    public void deleteRoutePoint(int id) {
        RoutePoint routePoint = routePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route point not found"));
        List<RouteDetail> routeDetails = routeDetailRepository.findAllByRoutePointId(routePoint.getId());
        if (routeDetails == null || routeDetails.isEmpty()) {
            try{
                routePoint.setDeleted(true);
                routePointRepository.save(routePoint);
            }catch (Exception e){
                throw new RuntimeException("Có lỗi xảy ra khi xóa điểm dừng");
            }
        } else {
            throw new RuntimeException("Không thể xóa điểm dừng vì nó đang được sử dụng trong các tuyến đường");
        }
    }
}
