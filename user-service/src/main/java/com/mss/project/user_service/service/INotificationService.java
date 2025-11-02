package com.mss.project.user_service.service;

import com.mss.project.user_service.dto.request.NotificationRequest;
import com.mss.project.user_service.dto.response.NotificationDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface INotificationService {

    void saveNotification(NotificationRequest notificationRequest);

    Page<NotificationDTO> getAllNotificationsByAuthenticatedUser();

    void markAsRead(int notificationId);

    void markAllAsRead();
}
