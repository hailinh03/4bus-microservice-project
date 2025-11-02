package com.mss.project.bus_service.controller;


import com.mss.project.bus_service.dto.request.BusDTO;
import com.mss.project.bus_service.dto.response.BusResponse;
import com.mss.project.bus_service.dto.response.BusListResponse;
import com.mss.project.bus_service.service.IBusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Tag(name = "Bus Management", description = "APIs for managing bus")
public class BusController {
    private final IBusService busService;

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "create new bus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusResponse> createBus(@RequestBody @Valid BusDTO busDTO) {
        return new ResponseEntity<>(busService.createBus(busDTO), HttpStatus.CREATED);
    };

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "get all bus")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PASSENGER') or hasRole('DRIVER')")
    public ResponseEntity<BusListResponse> getAllBuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String searchString
            ) {
        return ResponseEntity.ok(busService.getAllBuses(page, size, sortBy, sortDirection,searchString));
    }

    @GetMapping("/{id}")
    @Operation(summary = "get bus by id")
    public ResponseEntity<BusResponse> getBusById(@PathVariable int id) {
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "update bus by id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusResponse> updateBus(@PathVariable int id, @RequestBody @Valid BusDTO busDTO) {
        return ResponseEntity.ok(busService.updateBus(id, busDTO));
    }
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "delete bus by id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusResponse> deleteBus(@PathVariable int id) {
        return ResponseEntity.ok(busService.deleteBus(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search buses by name, description or category name")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PASSENGER') or hasRole('DRIVER')")
    public ResponseEntity<BusListResponse> searchBuses(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        BusListResponse response = busService.searchBuses(query, page, size);
        return ResponseEntity.ok(response);
    }
}
