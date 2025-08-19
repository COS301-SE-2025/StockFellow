package com.stockfellow.demoservice.dto;

public class RegistrationResponse {
    private String message;
    private String userId;
    private String error;
    
    // Constructors
    public RegistrationResponse() {}
    
    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
