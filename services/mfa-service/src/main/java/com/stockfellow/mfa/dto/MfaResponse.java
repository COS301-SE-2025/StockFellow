package com.stockfellow.mfa.dto;

public class MfaResponse {
    private boolean success;
    private String message;
    private String sessionToken;

    public MfaResponse() {
    }

    public MfaResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public MfaResponse(boolean success, String message, String sessionToken) {
        this.success = success;
        this.message = message;
        this.sessionToken = sessionToken;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
