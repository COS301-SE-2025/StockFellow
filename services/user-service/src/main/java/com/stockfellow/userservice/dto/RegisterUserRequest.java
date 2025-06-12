package com.stockfellow.userservice.dto;

public class RegisterUserRequest {
    private String name;
    private String email;
    private String saId;
    private String mobileNumber;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSaId() { return saId; }
    public void setSaId(String saId) { this.saId = saId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
}