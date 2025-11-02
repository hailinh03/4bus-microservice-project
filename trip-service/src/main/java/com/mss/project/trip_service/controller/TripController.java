package com.mss.project.trip_service.controller;

import com.mss.project.trip_service.dto.ApiResponse;
import com.mss.project.trip_service.dto.request.TripCreateRequest;
import com.mss.project.trip_service.dto.response.TripDTO;
import com.mss.project.trip_service.dto.response.TripListDTO;
import com.mss.project.trip_service.enums.TripSortField;
import com.mss.project.trip_service.enums.TripStatus;
import com.mss.project.trip_service.service.impl.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trip Service", description = "Endpoints for managing trips")
public class TripController {

    private final TripService tripService;

    @GetMapping
    @Operation(summary = "Get all trips",
            description = "This endpoint retrieves a paginated list of trips with optional filters.")
    public ResponseEntity<ApiResponse<TripListDTO>> getAllTrip(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer startProvinceId,
            @RequestParam(required = false) Integer endProvinceId,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(defaultValue = "START_TIME") TripSortField sortBy,
            @RequestParam(required = false) String searchString,
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime){
        TripListDTO trips = tripService.getAllTrips(
                searchString, startProvinceId, endProvinceId, startTime, status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.<TripListDTO>builder()
                .message("Lấy danh sách chuyến đi thành công")
                .success(true)
                .data(trips)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID",
            description = "This endpoint retrieves a trip by its ID.")
    public ResponseEntity<ApiResponse<TripDTO>> getTripById(@PathVariable int id) {
        TripDTO trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.<TripDTO>builder()
                .message("Lấy thông tin chuyến đi thành công")
                .success(true)
                .data(trip)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new trip",
            description = "This endpoint allows an admin to create a new trip.")
    public ResponseEntity<ApiResponse<TripDTO>> createTrip(@RequestBody TripCreateRequest tripCreateRequest){
        TripDTO createdTrip = tripService.createTrip(tripCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).
                body(ApiResponse.<TripDTO>builder()
                        .message("Tạo chuyến đi thành công")
                        .success(true)
                        .data(createdTrip)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a trip", description = "This endpoint allows an admin to update a trip.")
    public ResponseEntity<ApiResponse<Void>> updateTrip(@PathVariable int id, @RequestBody TripCreateRequest tripCreateRequest) {
        tripService.updateTrip(id, tripCreateRequest);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Cập nhật chuyến đi thành công")
                        .success(true)
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a trip", description = "This endpoint allows an admin to delete a trip.")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable int id) {
        tripService.deleteTrip(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Xóa chuyến đi thành công")
                        .success(true)
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping("/driver/{driverId}")
//    @PreAuthorize("hasRole('DRIVER')")
//    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all trips by driver ID",
            description = "This endpoint retrieves all trips for a specific driver with pagination and sorting options.")
    public ResponseEntity<?> getAllTripsByDriverId(
            @PathVariable int driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TripDTO> trips = tripService.getAllTripsByDriverId(driverId, page, size);
        return ResponseEntity.ok(trips);
    }
}
