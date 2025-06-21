package com.stockfellow.transactionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendPayoutNotification(String userId, String email, Double amount, String status, String transactionId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Payout Notification");
        message.setText(String.format(
            "Dear User,\n\nYour payout of R%.2f has been %s.\nTransaction ID: %s\n\nRegards,\nStockFellow Team",
            amount, status.toLowerCase(), transactionId));
        try {
            mailSender.send(message);
            logger.info("Payout notification sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send payout notification: {}", e.getMessage());
        }
    }

    public void sendSMS(String phone, String message) {
        // Mock SMS implementation (replace with Twilio or similar)
        logger.info("Sending SMS to {}: {}", phone, message);
    }
}
