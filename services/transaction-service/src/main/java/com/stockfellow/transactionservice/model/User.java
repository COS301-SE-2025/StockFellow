package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;

// User Entity
@Entity
@Table(name = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    
    @Id
    @Column(name = "user_id")
    @JsonProperty("id")
    private UUID userId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "first_name")
    @JsonProperty("first_name")
    private String firstName;
    
    @Column(name = "last_name")
    @JsonProperty("last_name")
    private String lastName;
    
    @Column(name = "phone")
    @JsonProperty("phone")
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient  // Don't persist to database
    @JsonProperty("customer_code")
    private String customerCode;
    
    @Transient
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Constructors
    public User() {}

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = UserStatus.ACTIVE;
    }

    // Getters
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Convenience method for full name
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    @PrePersist
    protected void onCreate() {
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // User Status Enum
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_VERIFICATION,
        BANNED
    }
}