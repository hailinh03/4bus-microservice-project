package com.mss.project.trip_service.controller;

import com.mss.project.trip_service.dto.ApiResponse;
import com.mss.project.trip_service.dto.request.RoutePointCreateRequest;
import com.mss.project.trip_service.dto.response.RoutePointDTO;
import com.mss.project.trip_service.dto.response.RoutePointListDTO;
import com.mss.project.trip_service.enums.RoutePointSortField;
import com.mss.project.trip_service.service.impl.RoutePointService;
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

@Controller
@RequestMapping("/api/route-points")
@RequiredArgsConstructor
@Tag(name = "Trip Service", description = "Operations related to route points")
public class RoutePointController {

    private final RoutePointService routePointService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new route point for a trip",
            description = "This endpoint allows an admin to create a new route point for a trip.")
    public ResponseEntity<ApiResponse<RoutePointDTO>> createRoutePoint(@RequestBody RoutePointCreateRequest routePointCreateRequest){
        RoutePointDTO createdRoutePoint = routePointService.createRoutePoint(routePointCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).
                body(ApiResponse.<RoutePointDTO>builder()
                        .message("Tạo điểm dừng thành công")
                        .success(true)
                        .data(createdRoutePoint)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all route points",
            description = "This endpoint allows an admin to retrieve all route points.")
    public ResponseEntity<ApiResponse<RoutePointListDTO>> getAllRoutePoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(defaultValue = "CREATED_AT") RoutePointSortField sortBy,
            @RequestParam(required = false) String searchString,
            @RequestParam(required = false, defaultValue = "true") boolean isActive
    ) {
        RoutePointListDTO routePointList = routePointService.getAllRoutePoints(
                searchString, page, size, sortBy, sortDir, isActive
        );
        return ResponseEntity.ok(ApiResponse.<RoutePointListDTO>builder()
                .message("Lấy danh sách điểm dừng thành công")
                .success(true)
                .data(routePointList)
                .build());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get route point by ID")
    public ResponseEntity<ApiResponse<RoutePointDTO>> getRoutePointById(@PathVariable int id) {
        RoutePointDTO routePoint = routePointService.getRoutePointById(id);
        return ResponseEntity.ok(ApiResponse.<RoutePointDTO>builder()
                .message("Lấy điểm dừng thành công")
                .success(true)
                .data(routePoint)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a route point",
            description = "This endpoint allows an admin to update an existing route point.")
    public ResponseEntity<ApiResponse<Void>> updateRoutePoint(@PathVariable int id,@RequestBody RoutePointCreateRequest routePointCreateRequest) {
        //Return 204 if the update is successful
        routePointService.updateRoutePoint(id, routePointCreateRequest);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Cập nhật điểm dừng thành công")
                        .success(true)
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a route point",
            description = "This endpoint allows an admin to delete an existing route point.")
    public ResponseEntity<ApiResponse<Void>> deleteRoutePoint(@PathVariable int id) {
        routePointService.deleteRoutePoint(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Xóa điểm dừng thành công")
                        .success(true)
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }
}
