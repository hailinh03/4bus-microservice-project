package com.mss.project.trip_service.service;

import com.mss.project.trip_service.config.FeignClientConfig;
import com.mss.project.trip_service.dto.ApiResponse;
import com.mss.project.trip_service.dto.response.BusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "booking-service",url = "${4BUS.service.booking-service.url}",
configuration = FeignClientConfig.class)
public interface IBookingService {
    @GetMapping("/api/tickets/booked-seats/{id}")
    ApiResponse<List<String>> getBookedSeatsByTripId(@PathVariable("id") Integer id);
    @PutMapping("/api/tickets/mark-used/{tripId}")
    ApiResponse<Void> markTicketsUsedByTripId(@PathVariable Integer tripId);
    @PutMapping("/api/tickets/mark-expired/{tripId}")
    ApiResponse<Void> markTicketsExpiredByTripId(@PathVariable Integer tripId);
}
