package com.stockfellow.mfa.dto;

public class MfaVerifyRequest {
    private String email;
    private String otpCode;

    public MfaVerifyRequest() {
    }

    public MfaVerifyRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
