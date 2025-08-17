package com.stockfellow.mfa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOTP(String toEmail, String otpCode, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("StockFellow - Your Verification Code");
            message.setText(buildOTPEmailBody(otpCode, userName));

            mailSender.send(message);
            logger.info("OTP email sent successfully to: {} via {}", toEmail, getEmailProvider());

        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {} via {}", toEmail, getEmailProvider(), e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }

    private String getEmailProvider() {
        if (mailUsername.contains("@gmail.com"))
            return "Gmail";
        if (mailUsername.contains("@outlook.com") || mailUsername.contains("@hotmail.com"))
            return "Outlook";
        if (mailUsername.contains("@yahoo.com"))
            return "Yahoo";
        if (mailUsername.contains("@icloud.com") || mailUsername.contains("@me.com"))
            return "iCloud";
        return "Custom SMTP";
    }

    private String buildOTPEmailBody(String otpCode, String userName) {
        return String.format(
                "Hi %s,\n\n" +
                        "Your StockFellow verification code is: %s\n\n" +
                        "This code will expire in 5 minutes.\n\n" +
                        "If you didn't request this code, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "StockFellow Team\n",
                userName != null ? userName : "", otpCode);
    }
}
