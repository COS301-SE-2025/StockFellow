package com.stockfellow.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId; // Keycloak ID
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "username")
    private String username;
    
    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", unique = true)
    private String email;
    
    @Size(max = 100)
    @Column(name = "first_name")
    private String firstName;
    
    @Size(max = 100)
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "email_verified")
    private boolean emailVerified = false;
    
    @Size(max = 20)
    @Column(name = "contact_number")
    private String contactNumber;
    
    @Size(max = 13)
    @Column(name = "id_number")
    private String idNumber;
    
    @Column(name = "id_verified")
    private boolean idVerified = false;
    
    @Column(name = "alfresco_document_id")
    private String alfrescoDocumentId = "none";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "date_of_birth")
    private String dateOfBirth;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "citizenship")
    private String citizenship;


    @Column(name = "affordability_tier")
    private Integer affordabilityTier = 0;

    @Column(name = "affordability_confidence")
    private Double affordabilityConfidence = 0.0;

    @Column(name = "affordability_analyzed_at")
    private Date affordabilityAnalyzedAt = null;
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
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
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getIdNumber() {
        return idNumber;
    }
    
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    
    public boolean isIdVerified() {
        return idVerified;
    }
    
    public void setIdVerified(boolean idVerified) {
        this.idVerified = idVerified;
    }
    
    public String getAlfrescoDocumentId() {
        return alfrescoDocumentId;
    }
    
    public void setAlfrescoDocumentId(String alfrescoDocumentId) {
        this.alfrescoDocumentId = alfrescoDocumentId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getCitizenship() {
        return citizenship;
    }
    
    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailVerified=" + emailVerified +
                ", contactNumber='" + contactNumber + '\'' +
                ", idNumber='" + (idNumber != null ? idNumber.substring(0, 6) + "XXXXXXX" : "null") + '\'' +
                ", idVerified=" + idVerified +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

     public Integer getAffordabilityTier() { 
        return affordabilityTier; 
    }
    
    public void setAffordabilityTier(Integer affordabilityTier) { 
        this.affordabilityTier = affordabilityTier; 
    }
    
    public Double getAffordabilityConfidence() { 
        return affordabilityConfidence; 
    }
    
    public void setAffordabilityConfidence(Double affordabilityConfidence) { 
        this.affordabilityConfidence = affordabilityConfidence; 
    }
    
    public Date getAffordabilityAnalyzedAt() { 
        return affordabilityAnalyzedAt; 
    }
    
    public void setAffordabilityAnalyzedAt(Date affordabilityAnalyzedAt) { 
        this.affordabilityAnalyzedAt = affordabilityAnalyzedAt; 
    }
}