package com.stockfellow.notificationservice.repository;

import com.stockfellow.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientIdOrRecipientIdIsNull(String recipientId);
}
