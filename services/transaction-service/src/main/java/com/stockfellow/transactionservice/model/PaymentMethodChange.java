package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_method_changes")
public class PaymentMethodChange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "change_id")
    private UUID changeId;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "old_method_id")
    private UUID oldMethodId;
    
    @Column(name = "new_method_id", nullable = false)
    private UUID newMethodId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "flagged", nullable = false)
    private boolean flagged = false;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public PaymentMethodChange() {}

    public PaymentMethodChange(String userId, UUID oldMethodId, UUID newMethodId) {
        this.userId = userId;
        this.oldMethodId = oldMethodId;
        this.newMethodId = newMethodId;
        this.timestamp = LocalDateTime.now();
        this.flagged = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getChangeId() { return changeId; }
    public void setChangeId(UUID changeId) { this.changeId = changeId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public UUID getOldMethodId() { return oldMethodId; }
    public void setOldMethodId(UUID oldMethodId) { this.oldMethodId = oldMethodId; }
    
    public UUID getNewMethodId() { return newMethodId; }
    public void setNewMethodId(UUID newMethodId) { this.newMethodId = newMethodId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        if (changeId == null) {
            changeId = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
