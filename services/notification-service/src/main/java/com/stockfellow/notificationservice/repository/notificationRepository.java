package com.stockfellow.notificationservice.repository;

import com.stockfellow.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimestampDesc(String userId);

    List<Notification> findByUserIdAndIsReadOrderByTimestampDesc(String userId, boolean isRead);

    long countByUserIdAndIsRead(String userId, boolean isRead);
}
