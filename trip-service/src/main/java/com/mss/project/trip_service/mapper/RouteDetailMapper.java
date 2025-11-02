package com.mss.project.trip_service.mapper;

import com.mss.project.trip_service.dto.response.RouteDetailDTO;
import com.mss.project.trip_service.entity.RouteDetail;

import java.util.List;

public class RouteDetailMapper {
    public static RouteDetailDTO toRouteDetailDTO(RouteDetail routeDetail) {
        if (routeDetail == null) {
            return null;
        }

        return RouteDetailDTO.builder()
                .routePoint(RoutePointMapper.toRoutePointDTO(routeDetail.getRoutePoint()))
                .orderIndex(routeDetail.getOrderIndex())
                .durationFromPreviousPoint(routeDetail.getDurationFromPreviousPoint())
                .build();
    }

    public static List<RouteDetailDTO> toRouteDetailDTOList(List<RouteDetail> routeDetails) {
        if (routeDetails == null || routeDetails.isEmpty()) {
            return List.of();
        }

        return routeDetails.stream()
                .map(RouteDetailMapper::toRouteDetailDTO)
                .toList();
    }
}
