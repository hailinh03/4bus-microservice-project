package com.mss.project.trip_service.service.impl;

import com.mss.project.trip_service.dto.request.RouteCreateRequest;
import com.mss.project.trip_service.dto.request.RouteDetailCreateRequest;
import com.mss.project.trip_service.dto.response.RouteDTO;
import com.mss.project.trip_service.dto.response.RouteListDTO;
import com.mss.project.trip_service.entity.Route;
import com.mss.project.trip_service.entity.RouteDetail;
import com.mss.project.trip_service.entity.RoutePoint;
import com.mss.project.trip_service.entity.Trip;
import com.mss.project.trip_service.enums.RouteSortField;
import com.mss.project.trip_service.mapper.RouteMapper;
import com.mss.project.trip_service.repository.RouteDetailRepository;
import com.mss.project.trip_service.repository.RoutePointRepository;
import com.mss.project.trip_service.repository.RouteRepository;
import com.mss.project.trip_service.repository.TripRepository;
import com.mss.project.trip_service.service.IRouteService;
import com.mss.project.trip_service.specification.RouteSpecification;
import com.mss.project.trip_service.utils.Prechecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService implements IRouteService {

    private final RouteRepository routeRepository;
    private final RoutePointRepository routePointRepository;
    private final RouteDetailRepository routeDetailRepository;
    private final TripRepository tripRepository;

    @Override
    public RouteListDTO getRoutes(String searchString, Integer page, Integer size, RouteSortField sortBy, Sort.Direction sortDir, boolean isActive) {
        Prechecker.checkPaginationParameters(page, size);
        Specification<Route> spec = RouteSpecification.hasSearchString(searchString)
                .and(RouteSpecification.hasActive(isActive))
                .and(RouteSpecification.hasDeleted(false));
        Pageable pageable = PageRequest.of(page, size, sortDir, sortBy.getFieldName());
        Page<Route> routePage = routeRepository.findAll(spec, pageable);
        return RouteMapper.toRouteListDTO(routePage);
    }

    @Override
    public RouteDTO createRoute(RouteCreateRequest routeCreateRequest) {
        List<RouteDetailCreateRequest> routeDetails = routeCreateRequest.getRoutePoints();
        if (routeDetails == null || routeDetails.size()<=1) {
            throw new IllegalArgumentException("Route details must contain at least two route points.");
        }
        try {
            Route route = new Route();
            RoutePoint currentRoutePoint;
            List<RouteDetail> routeDetailList = new ArrayList<>();
            for (RouteDetailCreateRequest routeDetailRequest : routeDetails) {
                // Get the route point by ID
                currentRoutePoint = routePointRepository.findById(routeDetailRequest.getRoutePointId())
                        .orElseThrow(() -> new IllegalArgumentException("Route point with ID " + routeDetailRequest.getRoutePointId() + " does not exist."));
                // Create a new RouteDetail entity and save it
                RouteDetail routeDetail = new RouteDetail();
                routeDetail.setRoutePoint(currentRoutePoint);
                routeDetail.setOrderIndex(routeDetail.getOrderIndex());
                routeDetail.setDurationFromPreviousPoint(routeDetail.getDurationFromPreviousPoint());
                RouteDetail savedRouteDetail = routeDetailRepository.save(routeDetail);
                routeDetailList.add(savedRouteDetail);
            }
            // Set the route details to the route entity
            route.setRouteDetails(routeDetailList);

            // Set other route properties
            route.setName(routeCreateRequest.getName());
            route.setOrigin(routeCreateRequest.getOrigin());
            route.setDestination(routeCreateRequest.getDestination());
            route.setDescription(routeCreateRequest.getDescription());
            route.setDistance(routeCreateRequest.getDistance());
            route.setDuration(routeCreateRequest.getDuration());
            route.setTotalRoutePoints(routeCreateRequest.getTotalRoutePoints());
            route = routeRepository.save(route);
            return RouteMapper.toRouteDTO(route);
        }catch (Exception e) {
            throw new RuntimeException("Error while creating route details: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateRoute(int id, RouteCreateRequest routeCreateRequest) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tuyến đường"));
        List<RouteDetail> currentRouteDetails = route.getRouteDetails();
        try{
            routeDetailRepository.deleteAll(currentRouteDetails);
        }catch (Exception e) {
            log.error("Error deleting current route details: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi xóa các chi tiết tuyến đường hiện tại");
        }
        List<RouteDetailCreateRequest> routeDetails = routeCreateRequest.getRoutePoints();
        if (routeDetails == null || routeDetails.size() <= 1) {
            throw new RuntimeException("Tuyến đường phải có ít nhất hai điểm.");
        }
        List<RouteDetail> routeDetailList = new ArrayList<>();
        for (RouteDetailCreateRequest routeDetailRequest : routeDetails) {
            RoutePoint currentRoutePoint = routePointRepository.findById(routeDetailRequest.getRoutePointId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm dừng"));
            RouteDetail routeDetail = new RouteDetail();
            routeDetail.setRoutePoint(currentRoutePoint);
            routeDetail.setOrderIndex(routeDetailRequest.getOrderIndex());
            routeDetail.setDurationFromPreviousPoint(routeDetailRequest.getDurationFromPreviousPoint());
            RouteDetail savedRouteDetail = routeDetailRepository.save(routeDetail);
            routeDetailList.add(savedRouteDetail);
        }
        try{
            route.setRouteDetails(routeDetailList);
            route.setName(routeCreateRequest.getName());
            route.setOrigin(routeCreateRequest.getOrigin());
            route.setDestination(routeCreateRequest.getDestination());
            route.setDescription(routeCreateRequest.getDescription());
            route.setDistance(routeCreateRequest.getDistance());
            route.setDuration(routeCreateRequest.getDuration());
            route.setTotalRoutePoints(routeCreateRequest.getTotalRoutePoints());
            routeRepository.save(route);
        }catch (Exception e) {
            log.error("Error updating route: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi cập nhật tuyến đường");
        }

    }

    @Override
    public void deleteRoute(int id) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Route with ID " + id + " not found."));
        List<Trip> trips = tripRepository.findAllByRouteId(route.getId());
        if (trips == null || trips.isEmpty()) {
            try {
                route.setDeleted(true);
                routeRepository.save(route);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi xóa tuyến đường");
            }
        }else {
            throw new RuntimeException("Không thể xóa tuyến đường vì có chuyến đi đã sử dụng tuyến này.");
        }
    }
}
