package com.stockfellow.mfa.config;

import com.stockfellow.mfa.service.EmailService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@TestConfiguration
@Profile("test")
public class TestConfig {

    private final Map<String, String> sentOtps = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public EmailService mockEmailService() {
        EmailService mockEmailService = Mockito.mock(EmailService.class);

        Mockito.doAnswer(invocation -> {
            String email = invocation.getArgument(0);
            String otp = invocation.getArgument(1);
            String userName = invocation.getArgument(2);

            sentOtps.put(email, otp);

            System.out.println("Mock Email Service: Would send OTP " + otp + " to " + email + " for user " + userName);
            return null;
        }).when(mockEmailService).sendOTP(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        return mockEmailService;
    }

    @Bean
    public TestOtpCapture testOtpCapture() {
        return new TestOtpCapture(sentOtps);
    }

    // Helper to capture otps sent during tests

    public static class TestOtpCapture {
        private final Map<String, String> sentOtps;

        public TestOtpCapture(Map<String, String> sentOtps) {
            this.sentOtps = sentOtps;
        }

        public String getLastOtpForEmail(String email) {
            return sentOtps.get(email);
        }

        public void clearOtps() {
            sentOtps.clear();
        }

        public boolean hasOtpForEmail(String email) {
            return sentOtps.containsKey(email);
        }
    }
}
