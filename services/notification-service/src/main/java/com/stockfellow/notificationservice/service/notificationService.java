package com.stockfellow.notificationservice.service;

import com.stockfellow.notificationservice.model.Notification;
import com.stockfellow.notificationservice.model.NotificationType;
import com.stockfellow.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(String userId, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByTimestampDesc(userId, false);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
