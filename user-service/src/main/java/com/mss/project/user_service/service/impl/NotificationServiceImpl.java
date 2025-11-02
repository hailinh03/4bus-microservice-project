package com.mss.project.user_service.service.impl;

import com.mss.project.user_service.dto.request.NotificationRequest;
import com.mss.project.user_service.dto.response.NotificationDTO;
import com.mss.project.user_service.entity.Notification;
import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.mapper.NotificationMapper;
import com.mss.project.user_service.repository.NotificationRepository;
import com.mss.project.user_service.repository.UserRepository;
import com.mss.project.user_service.service.INotificationService;
import com.mss.project.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final IUserService userService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void saveNotification(NotificationRequest notificationRequest) {

        Notification notification = new Notification();
        notification.setTitle(notificationRequest.getTitle());
        notification.setContent(notificationRequest.getContent());
        notification.setUrl(notificationRequest.getUrl());
        notification.setUser(userRepository.findById(notificationRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + notificationRequest.getUserId())));
        notification.setSystem(true);
        Notification saveNotification = notificationRepository.save(notification);

        NotificationDTO notificationDTO = notificationMapper.toNotificationDTO(saveNotification);
        simpMessagingTemplate.convertAndSend("/topic/notification/"+saveNotification.getUser().getId(), notificationDTO);

    }

    @Override
    public Page<NotificationDTO> getAllNotificationsByAuthenticatedUser() {
        User user = userService.getAuthenticatedUser();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPaging = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(),pageable);
//        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
//        return notifications.stream().map(notificationMapper::toNotificationDTO).toList();

        return notificationPaging.map(notificationMapper::toNotificationDTO);
    }

    @Override
    public void markAsRead(int notificationId) {
        User user = userService.getAuthenticatedUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo: " + notificationId));
        if(notification.getUser().getId() != user.getId()) {
            throw new RuntimeException("Bạn không có quyền truy cập thông báo này");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead() {
        User user = userService.getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}
