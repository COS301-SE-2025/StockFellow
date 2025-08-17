package com.stockfellow.mfa.entity;

import java.time.LocalDateTime;

public class OTPRecord {
    private String otpCode;
    private LocalDateTime expiryTime;
    private String userId;
    private boolean verified;

    public OTPRecord(String otpCode, String userId, int expiryMinutes) {
        this.otpCode = otpCode;
        this.userId = userId;
        this.expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
        this.verified = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    // Getters and setters
    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
