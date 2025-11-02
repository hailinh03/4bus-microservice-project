package com.mss.project.trip_service.controller;

import com.mss.project.trip_service.dto.ApiResponse;
import com.mss.project.trip_service.dto.request.RouteCreateRequest;
import com.mss.project.trip_service.dto.response.RouteDTO;
import com.mss.project.trip_service.dto.response.RouteListDTO;
import com.mss.project.trip_service.enums.RouteSortField;
import com.mss.project.trip_service.enums.TripStatus;
import com.mss.project.trip_service.service.impl.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Trip Service", description = "APIs for managing routes")
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all route points",
            description = "This endpoint allows an admin to retrieve all route points.")
    public ResponseEntity<ApiResponse<RouteListDTO>> getAllRoutePointsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(defaultValue = "CREATED_AT") RouteSortField sortBy,
            @RequestParam(required = false) String searchString,
            @RequestParam(required = false, defaultValue = "true") boolean isActive
    ) {
        RouteListDTO routeList = routeService.getRoutes(
                searchString, page, size, sortBy, sortDir, isActive
        );
        return ResponseEntity.ok(ApiResponse.<RouteListDTO>builder()
                .message("Lấy danh sách tuyến đường thành công")
                .success(true)
                .data(routeList)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new route",
            description = "This endpoint allows an admin to create a new route.")
    public ResponseEntity<ApiResponse<RouteDTO>> createRoute(@RequestBody RouteCreateRequest routeCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(ApiResponse.<RouteDTO>builder()
                        .message("Tạo tuyến đường thành công")
                        .success(true)
                        .data(routeService.createRoute(routeCreateRequest))
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a route", description = "This endpoint allows an admin to update a route.")
    public ResponseEntity<ApiResponse<Void>> updateRoute(@PathVariable int id, @RequestBody RouteCreateRequest routeCreateRequest) {
        routeService.updateRoute(id, routeCreateRequest);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Cập nhật tuyến đường thành công")
                        .success(true)
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a route", description = "This endpoint allows an admin to delete a route.")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable int id) {
        routeService.deleteRoute(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Xóa tuyến đường thành công")
                        .success(true)
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }
}
