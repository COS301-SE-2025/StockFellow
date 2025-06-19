package com.stockfellow.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.notificationservice.model.Notification;
import com.stockfellow.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final GroupServiceClient groupServiceClient;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               GroupServiceClient groupServiceClient,
                               JmsTemplate jmsTemplate,
                               ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.groupServiceClient = groupServiceClient;
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSystemNotification(String title, String message, String channel) {
        Notification notification = new Notification();
        notification.setType("SYSTEM");
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setStatus("UNREAD");
        notification.setChannel(channel);
        notification.setCreatedAt(Instant.now());

        if ("IN_APP".equals(channel)) {
            notificationRepository.save(notification);
        } else if ("EMAIL".equals(channel)) {
            logger.info("Sending EMAIL notification: {} - {}", title, message);
            // TODO: Integrate with SMTP service
        } else if ("PUSH".equals(channel)) {
            logger.info("Sending PUSH notification: {} - {}", title, message);
            // TODO: Integrate with Firebase/OneSignal
        }
    }

    public void sendGroupNotification(String groupId, String title, String message, String channel) {
        if (!groupServiceClient.groupExists(groupId)) {
            throw new IllegalArgumentException("Group not found");
        }

        List<String> memberIds = groupServiceClient.getGroupMemberIds(groupId);

        for (String recipientId : memberIds) {
            Notification notification = new Notification();
            notification.setType("GROUP");
            notification.setRecipientId(recipientId);
            notification.setGroupId(groupId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus("UNREAD");
            notification.setChannel(channel);
            notification.setCreatedAt(Instant.now());

            if ("IN_APP".equals(channel)) {
                notificationRepository.save(notification);
            } else if ("EMAIL".equals(channel)) {
                logger.info("Sending EMAIL to {}: {} - {}", recipientId, title, message);
                // TODO: Integrate with SMTP service
            } else if ("PUSH".equals(channel)) {
                logger.info("Sending PUSH to {}: {} - {}", recipientId, title, message);
                // TODO: Integrate with Firebase/OneSignal
            }
        }
    }

    public void sendTransactionNotification(String recipientId, String groupId, String transactionId, String title, String message, String channel) {
        if (groupId != null && !groupServiceClient.groupExists(groupId)) {
            throw new IllegalArgumentException("Group not found");
        }

        Notification notification = new Notification();
        notification.setType("TRANSACTION");
        notification.setRecipientId(recipientId);
        notification.setGroupId(groupId);
        notification.setTransactionId(transactionId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setStatus("UNREAD");
        notification.setChannel(channel);
        notification.setCreatedAt(Instant.now());

        if ("IN_APP".equals(channel)) {
            notificationRepository.save(notification);
        } else if ("EMAIL".equals(channel)) {
            logger.info("Sending EMAIL to {}: {} - {}", recipientId, title, message);
            // TODO: Integrate with SMTP service
        } else if ("PUSH".equals(channel)) {
            logger.info("Sending PUSH to {}: {} - {}", recipientId, title, message);
            // TODO: Integrate with Firebase/OneSignal
        }
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByRecipientIdOrRecipientIdIsNull(userId);
    }

    @JmsListener(destination = "group.events")
    public void handleGroupEvent(TextMessage message) throws JMSException {
        try {
            String json = message.getText();
            Map<String, Object> event = objectMapper.readValue(json, Map.class);
            String eventType = message.getStringProperty("eventType");

            switch (eventType) {
                case "GroupCreated":
                    String groupId = (String) event.get("groupId");
                    String name = (String) event.get("name");
                    List<String> memberIds = (List<String>) event.get("memberIds");
                    String adminId = (String) event.get("adminId");
                    memberIds.add(adminId); // Include admin in notifications
                    String title = "Group Created";
                    String messageText = String.format("You are part of %s!", name);
                    sendGroupNotification(groupId, title, messageText, "IN_APP");
                    break;
                case "GroupJoined":
                    groupId = (String) event.get("groupId");
                    String userId = (String) event.get("userId");
                    name = groupServiceClient.getGroupName(groupId);
                    title = "New Member Joined";
                    messageText = String.format("User %s joined %s!", userId, name);
                    sendGroupNotification(groupId, title, messageText, "IN_APP");
                    break;
                default:
                    logger.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Error processing group event: {}", e.getMessage(), e);
            throw new JMSException("Failed to process message: " + e.getMessage());
        }
    }
}