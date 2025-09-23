package com.stockfellow.mfa.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.Base64;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String APPLICATION_NAME = "StockFellow MFA";
    private static final String GMAIL_SEND_SCOPE = "https://www.googleapis.com/auth/gmail.send";

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${google.service.account.key.path}")
    private String serviceAccountKeyPath;

    private Gmail gmailService;

    @PostConstruct
    public void init() throws Exception {
        this.gmailService = createGmailService();
        logger.info("Gmail API service initialized successfully");
    }

    private Gmail createGmailService() throws Exception {
        try {
            // Load service account credentials
            GoogleCredentials credentials = ServiceAccountCredentials
                    .fromStream(new FileInputStream(serviceAccountKeyPath))
                    .createScoped(Collections.singletonList(GMAIL_SEND_SCOPE));

            // Build Gmail service
            return new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            logger.error("Failed to initialize Gmail service", e);
            throw new RuntimeException("Failed to initialize Gmail service", e);
        }
    }

    public void sendOTP(String toEmail, String otpCode, String userName) {
        try {
            MimeMessage mimeMessage = createOTPEmail(toEmail, otpCode, userName);
            Message message = createMessageWithEmail(mimeMessage);
            
            // Send the message
            gmailService.users().messages().send("me", message).execute();
            logger.info("OTP email sent successfully to: {} via Gmail API", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {} via Gmail API", toEmail, e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }

    private MimeMessage createOTPEmail(String toEmail, String otpCode, String userName) throws Exception {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(fromEmail));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
        email.setSubject("StockFellow - Your Verification Code");
        email.setText(buildOTPEmailBody(otpCode, userName));

        return email;
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
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