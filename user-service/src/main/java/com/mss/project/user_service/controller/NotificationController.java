package com.mss.project.user_service.controller;

import com.mss.project.user_service.dto.request.NotificationRequest;
import com.mss.project.user_service.service.INotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotificationsByAuthenticatedUser());
    }

    @PutMapping("/read/{notificationId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> markAsRead(@PathVariable int notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read successfully.");
    }

    @PutMapping("/mark-all-read")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok("All notifications marked as read successfully.");
    }

    @PostMapping("/send-notification")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        try {
            notificationService.saveNotification(request);
            return ResponseEntity.ok("Notification sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send notification: " + e.getMessage());
        }
    }
}
