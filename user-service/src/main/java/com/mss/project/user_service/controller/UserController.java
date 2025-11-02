package com.mss.project.user_service.controller;

import com.mss.project.user_service.dto.request.CreateUserRequest;
import com.mss.project.user_service.dto.response.UserDTO;
import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.enums.Role;
import com.mss.project.user_service.enums.UserSortField;
import com.mss.project.user_service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get authenticated user info")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<User> getAuthenticatedUser() {
        return ResponseEntity.ok(userService.getAuthenticatedUser());
    }

    @Operation(summary = "Get user info by UserId")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Register a new user")
    @PostMapping
    public ResponseEntity<UserDTO> register(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update authenticated user info")
    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@RequestBody Map<String, Object>  request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update authenticated user's password")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok("Mật khẩu đã được cập nhật thành công");
    }

    @Operation(summary = "Get all drivers")
    @GetMapping("/valid-drivers")
    public ResponseEntity<List<UserDTO>> getAllDrivers() {
        return ResponseEntity.ok(userService.getAllDrivers());
    }

    @Operation(summary = "Get drivers by IDs", description = "This endpoint is used to validate driver IDs.")
    @GetMapping("/drivers/validate")
    public ResponseEntity<List<UserDTO>> getDriversByIds(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(userService.getDriversByIds(ids));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new driver")
    @PostMapping("/drivers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createDriver(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createDriver(request));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all users")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "DESC", name = "sortDir") Sort.Direction sortDir,
            @RequestParam(defaultValue = "USERNAME", name = "sortBy") UserSortField sortBy,
            @RequestParam(required = false, name = "searchString") String searchString,
            @RequestParam(defaultValue = "PASSENGER", name = "role") Role role,
            @RequestParam(required = false, defaultValue = "false", name = "isDeleted") boolean isDeleted
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortDir, sortBy, searchString, role, isDeleted));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete User")
    @PutMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Unactive Or Active User")
    @PutMapping("/active-actions/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unactiveUser(@PathVariable int userId) {
        userService.activeAction(userId);
        return ResponseEntity.ok("User update status successfully");
    }

}
