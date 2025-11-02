package com.mss.project.user_service.repository;

import com.mss.project.user_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserId(int userId);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(int userId, Pageable pageable);
}
