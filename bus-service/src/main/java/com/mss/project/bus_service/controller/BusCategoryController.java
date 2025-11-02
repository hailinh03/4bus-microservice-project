package com.mss.project.bus_service.controller;

import com.mss.project.bus_service.dto.request.BusCategoryRequest;
import com.mss.project.bus_service.dto.response.BusCategoryListResponse;
import com.mss.project.bus_service.dto.response.BusCategoryResponse;
import com.mss.project.bus_service.service.IBusCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/bus-categories")
@RequiredArgsConstructor
@Tag(name = "Bus Category Management", description = "APIs for managing bus categories")
public class BusCategoryController {
    private final IBusCategoryService busCategoryService;


    @PostMapping
    @Operation(summary = "Create a new bus category")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusCategoryResponse> createBusCategory(@RequestBody @Valid BusCategoryRequest request) {
        return new ResponseEntity<>(busCategoryService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all bus categories")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusCategoryListResponse> getAllBusCategories(   @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                          @RequestParam(defaultValue = "asc") String sortDirection,
                                                                          @RequestParam(required = false) String searchString) {
        return ResponseEntity.ok(busCategoryService.getAllBusCategories(page,size, sortBy, sortDirection, searchString));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bus category by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusCategoryResponse> getBusCategoryById(@PathVariable int id) {
        return ResponseEntity.ok(busCategoryService.getBusCategoryById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a bus category by ID")
    public ResponseEntity<BusCategoryResponse> deleteCategory(@PathVariable int id) {
        BusCategoryResponse response = busCategoryService.deleteCategory(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a bus category by ID")
    public ResponseEntity<BusCategoryResponse> updateCategory(@PathVariable int id, @RequestBody BusCategoryRequest request) {
        BusCategoryResponse response = busCategoryService.updateCategory(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search bus categories by name or description")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PASSENGER') or hasRole('DRIVER')")
    public ResponseEntity<BusCategoryListResponse> searchBusCategories(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        BusCategoryListResponse response = busCategoryService.searchBusCategories(query, page, size);
        return ResponseEntity.ok(response);
    }

}
