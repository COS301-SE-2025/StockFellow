package com.stockfellow.mfa.dto;

public class MfaRequest {
    private String email;
    private String userId;

    public MfaRequest() {
    }

    public MfaRequest(String email, String userId) {
        this.email = email;
        this.userId = userId;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
