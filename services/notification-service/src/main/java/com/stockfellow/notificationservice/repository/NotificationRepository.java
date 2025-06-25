package com.stockfellow.notificationservice.repository;

import com.stockfellow.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Optional<Notification> findByNotificationId(String notificationId);
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Notification> findByUserIdAndReadStatusOrderByCreatedAtDesc(String userId, Boolean readStatus);
    
    List<Notification> findByGroupIdOrderByCreatedAtDesc(String groupId);
    
    List<Notification> findByStatusOrderByCreatedAtAsc(String status);
    
    List<Notification> findByTypeAndStatusOrderByCreatedAtAsc(String type, String status);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :fromDate ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndCreatedAtAfter(@Param("userId") String userId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.readStatus = false")
    Long countUnreadByUserId(@Param("userId") String userId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < 3 ORDER BY n.createdAt ASC")
    List<Notification> findFailedNotificationsForRetry();
    
    @Query("SELECT n FROM Notification n WHERE n.channel = :channel AND n.status = 'PENDING' ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findPendingNotificationsByChannel(@Param("channel") String channel);
}