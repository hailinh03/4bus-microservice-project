package com.mss.project.trip_service.mapper;

import com.mss.project.trip_service.dto.response.BusDTO;
import com.mss.project.trip_service.dto.response.TripDTO;
import com.mss.project.trip_service.dto.response.UserDTO;
import com.mss.project.trip_service.entity.Trip;

import java.util.List;

public class TripMapper {
    /**
     * Converts a Trip entity to a TripDTO.
     *
     * @param trip   the Trip entity to convert
     * @param bus    the BusDTO associated with the trip
     * @param drivers the list of UserDTOs representing the drivers of the trip
     * @return a TripDTO containing the trip details
     */
    public static TripDTO toTripDTO(Trip trip, BusDTO bus, List<UserDTO> drivers, List<String> bookedSeats) {
        return TripDTO.builder()
                .id(trip.getId())
                .name(trip.getName())
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .estimateDuration(trip.getEstimateDuration())
                .bus(bus)
                .drivers(drivers)
                .bookedSeats(bookedSeats)
                .route(RouteMapper.toRouteDTO(trip.getRoute()))
                .from(RouteDetailMapper.toRouteDetailDTO(trip.getRoute().getRouteDetails().getFirst()))
                .to(RouteDetailMapper.toRouteDetailDTO(trip.getRoute().getRouteDetails().getLast(
                )))
                .startTime(trip.getStartTime())
                .isHoliday(trip.isHoliday())
                .status(trip.getStatus().name())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    public static TripDTO toDto(Trip trip){
        return TripDTO.builder()
                .id(trip.getId())
                .name(trip.getName())
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .estimateDuration(trip.getEstimateDuration())
                .startTime(trip.getStartTime())
                .isHoliday(trip.isHoliday())
                .status(trip.getStatus().name())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

}
