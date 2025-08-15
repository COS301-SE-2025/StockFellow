package com.stockfellow.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserSyncRequest {
    
    @JsonProperty("keycloakId")
    @NotBlank(message = "Keycloak ID is required")
    private String keycloakId;
    
    @JsonProperty("username")
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    private String username;
    
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;
    
    @JsonProperty("firstName")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;
    
    @JsonProperty("lastName")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;
    
    @JsonProperty("emailVerified")
    private Boolean emailVerified;
    
    @JsonProperty("phoneNumber")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;
    
    @JsonProperty("idNumber")
    @Size(max = 13, message = "ID number cannot exceed 13 characters")
    private String idNumber;
    
    // Constructors
    public UserSyncRequest() {}
    
    public UserSyncRequest(String keycloakId, String username, String email, 
                          String firstName, String lastName) {
        this.keycloakId = keycloakId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getters and Setters
    public String getKeycloakId() {
        return keycloakId;
    }
    
    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getIdNumber() {
        return idNumber;
    }
    
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    
    @Override
    public String toString() {
        return "UserSyncRequest{" +
                "keycloakId='" + keycloakId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailVerified=" + emailVerified +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", idNumber='" + (idNumber != null ? idNumber.substring(0, Math.min(6, idNumber.length())) + "XXXXXXX" : "null") + '\'' +
                '}';
    }
}