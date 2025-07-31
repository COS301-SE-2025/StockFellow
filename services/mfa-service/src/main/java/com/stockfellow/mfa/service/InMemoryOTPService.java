package com.stockfellow.mfa.service;

import com.stockfellow.mfa.entity.OTPRecord;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class InMemoryOTPService {

    private final ConcurrentHashMap<String, OTPRecord> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 5;

    public InMemoryOTPService() {
        // Clean up expired OTPs every minute
        scheduler.scheduleAtFixedRate(this::cleanupExpiredOTPs, 1, 1, TimeUnit.MINUTES);
    }

    public String generateOTP(String email, String userId) {
        String otpCode = String.format("%06d", random.nextInt(1000000));
        OTPRecord record = new OTPRecord(otpCode, userId, EXPIRY_MINUTES);

        // Remove any existing OTP for this email
        otpStore.remove(email);

        // Store new OTP
        otpStore.put(email, record);

        return otpCode;
    }

    public boolean verifyOTP(String email, String providedOTP) {
        OTPRecord record = otpStore.get(email);

        if (record == null) {
            return false; // No OTP found for email
        }

        if (record.isExpired()) {
            otpStore.remove(email); // Clean up expired OTP
            return false;
        }

        if (record.getOtpCode().equals(providedOTP)) {
            record.setVerified(true);
            otpStore.remove(email); // Remove OTP after successful verification
            return true;
        }

        return false;
    }

    public boolean hasValidOTP(String email) {
        OTPRecord record = otpStore.get(email);
        return record != null && !record.isExpired();
    }

    public void invalidateOTP(String email) {
        otpStore.remove(email);
    }

    private void cleanupExpiredOTPs() {
        otpStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public int getActiveOTPCount() {
        return otpStore.size();
    }
}
