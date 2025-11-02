package com.mss.project.trip_service.controller;

import com.mss.project.trip_service.dto.ApiResponse;
import com.mss.project.trip_service.dto.request.CreateDriverRequest;
import com.mss.project.trip_service.dto.request.TripCreateRequest;
import com.mss.project.trip_service.dto.response.TripDTO;
import com.mss.project.trip_service.dto.response.UserDTO;
import com.mss.project.trip_service.service.IDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Trip Service", description = "APIs for managing drivers")
public class DriverController {

    private final IDriverService driverService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new driver",
            description = "This endpoint allows an admin to create a new driver.")
    public ResponseEntity<ApiResponse<UserDTO>> createDriver(@RequestBody CreateDriverRequest createDriverRequest){
        UserDTO createdDriverDTO = driverService.createDriver(createDriverRequest);
        return ResponseEntity.status(HttpStatus.CREATED).
                body(ApiResponse.<UserDTO>builder()
                        .message("Tạo tài xế thành công")
                        .success(true)
                        .data(createdDriverDTO)
                        .build());
    }

    @DeleteMapping("/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a driver",
            description = "This endpoint allows an admin to delete a driver by ID. " +
                    "The driver must not have any active trips.")
    public ResponseEntity<ApiResponse<UserDTO>> deleteDriver(@PathVariable int driverId) {
        UserDTO deletedDriverDTO = driverService.deleteDriver(driverId);
        return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                .message("Xóa tài xế thành công")
                .success(true)
                .data(deletedDriverDTO)
                .build());
    }

    @PutMapping("/action/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a driver status",
            description = "This endpoint allows an admin to update a driver by ID. " +
                    "The driver could be activated or deactivated.")
    public ResponseEntity<ApiResponse<UserDTO>> updateDriverStatus(@PathVariable int driverId) {
        UserDTO updateDriverStatus = driverService.updateDriverStatus(driverId);
        return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                .message("Cập nhật trạng thái tài xế thành công")
                .success(true)
                .data(updateDriverStatus)
                .build());
    }
}
