package com.stockfellow.mfa.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.sendgrid.api-key}")
    private String sendgridApiKey;

    public void sendOTP(String toEmail, String otpCode, String userName) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);

        String htmlBody = String.format(
                "<html><body>" +
                        "<p>Hi %s,</p>" +
                        "<p>Your StockFellow verification code is: <b>%s</b></p>" +
                        "<p>This code will expire in 5 minutes.</p>" +
                        "<p>If you didn't request this code, please ignore this email.</p>" +
                        "<p>Best regards,<br/>StockFellow Team</p>" +
                        "</body></html>",
                userName != null ? userName : "", otpCode);

        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, "StockFellow - Your Verification Code", to, content);

        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("Failed to send email: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error sending email via SendGrid", ex);
        }
    }

}
