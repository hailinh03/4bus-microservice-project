package com.mss.project.booking_service.service;
import com.mss.project.booking_service.payload.ApiResponse;
import com.mss.project.booking_service.payload.notification.NotificationRequest;
import com.mss.project.booking_service.payload.trip.TripDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "user-service", url = "${fourbus.service.user-service.url}")
public interface UserService {
    @PostMapping("/notifications/send-notification")
    String sendNotification(@RequestBody NotificationRequest request);
}