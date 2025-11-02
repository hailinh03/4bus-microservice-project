package com.mss.project.booking_service.service;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.trip.TripDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "trip-service", url = "${fourbus.service.trip-service.url}")
public interface TripService {
    @GetMapping("/api/trips/{id}")
    ApiResponse<TripDTO> getTripById(@PathVariable("id") int id);
}