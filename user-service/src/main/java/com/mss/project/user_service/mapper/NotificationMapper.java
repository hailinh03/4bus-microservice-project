package com.mss.project.user_service.mapper;

import com.mss.project.user_service.dto.response.NotificationDTO;
import com.mss.project.user_service.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toNotificationDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setTitle(notification.getTitle());
        notificationDTO.setContent(notification.getContent());
        notificationDTO.setUrl(notification.getUrl());
        notificationDTO.setSystem(notification.isSystem());
        notificationDTO.setCreatedAt(notification.getCreatedAt());
        notificationDTO.setRead(notification.isRead());

        return notificationDTO;
    }
}
