package com.mss.project.trip_service.mapper;

import com.mss.project.trip_service.dto.response.RouteDTO;
import com.mss.project.trip_service.dto.response.RouteListDTO;
import com.mss.project.trip_service.entity.Route;
import org.springframework.data.domain.Page;

import java.util.List;

public class RouteMapper {

    public static RouteDTO toRouteDTO(Route route) {
        if (route == null) {
            return null;
        }

        return RouteDTO.builder()
                .id(route.getId())
                .name(route.getName())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .description(route.getDescription())
                .distance(route.getDistance())
                .duration(route.getDuration())
                .totalRoutePoints(route.getTotalRoutePoints())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .isActive(route.isActive())
                .routePoints(RouteDetailMapper.toRouteDetailDTOList(route.getRouteDetails()))
                .build();
    }

    public static RouteListDTO toRouteListDTO(Page<Route> routePage) {
        if (routePage == null) {
            return null;
        }

        List<RouteDTO> routes = routePage.getContent().stream()
                .map(RouteMapper::toRouteDTO)
                .toList();

        return RouteListDTO.builder()
                .routes(routes)
                .totalElements(routePage.getTotalElements())
                .totalPages(routePage.getTotalPages())
                .pageNumber(routePage.getNumber())
                .pageSize(routePage.getSize())
                .build();
    }
}
