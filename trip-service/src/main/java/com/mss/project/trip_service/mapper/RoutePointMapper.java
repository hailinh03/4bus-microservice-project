package com.mss.project.trip_service.mapper;

import com.mss.project.trip_service.dto.response.RoutePointDTO;
import com.mss.project.trip_service.dto.response.RoutePointListDTO;
import com.mss.project.trip_service.entity.RoutePoint;
import org.springframework.data.domain.Page;

public class RoutePointMapper {

    public static RoutePointDTO toRoutePointDTO(RoutePoint routePoint) {
        if (routePoint == null) {
            return null;
        }
        return RoutePointDTO.builder()
                .id(routePoint.getId())
                .name(routePoint.getName())
                .description(routePoint.getDescription())
                .latitude(routePoint.getLatitude())
                .longitude(routePoint.getLongitude())
                .province(ProvinceMapper.toProvinceDTO(routePoint.getProvince()))
                .fullAddress(routePoint.getFullAddress())
                .createdAt(routePoint.getCreatedAt())
                .updatedAt(routePoint.getUpdatedAt())
                .isActive(routePoint.isActive())
                .build();
    }

    public static RoutePointListDTO toRoutePointListDTO(Page<RoutePoint> routePointPage) {
        if (routePointPage == null) {
            return null;
        }
        return RoutePointListDTO.builder()
                .routePoints(routePointPage.getContent().stream()
                        .map(RoutePointMapper::toRoutePointDTO)
                        .toList())
                .totalElements(routePointPage.getTotalElements())
                .totalPages(routePointPage.getTotalPages())
                .pageNumber(routePointPage.getNumber())
                .pageSize(routePointPage.getSize())
                .build();
    }

}
