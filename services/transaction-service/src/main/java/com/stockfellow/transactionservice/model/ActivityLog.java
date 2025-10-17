package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_log")
public class ActivityLog {
    
    @Id
    @Column(name = "log_id")
    private UUID logId;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "cycle_id")
    private UUID cycleId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
    
    @Column(name = "action", nullable = false)
    private String action;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", columnDefinition = "jsonb")
    private JsonNode oldData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", columnDefinition = "jsonb")
    private JsonNode newData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public ActivityLog(UUID userId, EntityType entityType, UUID entityId, String action) {
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
    }

    public ActivityLog(UUID userId, UUID cycleId, EntityType entityType, UUID entityId, 
                      String action, JsonNode oldData, JsonNode newData) {
        this.userId = userId;
        this.cycleId = cycleId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.oldData = oldData;
        this.newData = newData;
    }

    // Getters
    public UUID getLogId() { return logId; }
    public UUID getUserId() { return userId; }
    public UUID getCycleId() { return cycleId; }
    public EntityType getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getAction() { return action; }
    public JsonNode getOldData() { return oldData; }
    public JsonNode getNewData() { return newData; }
    public JsonNode getMetadata() { return metadata; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setLogId(UUID logId) { this.logId = logId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public void setAction(String action) { this.action = action; }
    public void setOldData(JsonNode oldData) { this.oldData = oldData; }
    public void setNewData(JsonNode newData) { this.newData = newData; }
    public void setMetadata(JsonNode metadata) { this.metadata = metadata; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Static factory method
    public static ActivityLog fromEntity(ActivityLog activityLog) {
        ActivityLog dto = new ActivityLog();
        dto.logId = activityLog.getLogId();
        dto.userId = activityLog.getUserId();
        dto.cycleId = activityLog.getCycleId();
        dto.entityType = activityLog.getEntityType();
        dto.entityId = activityLog.getEntityId();
        dto.action = activityLog.getAction();
        dto.oldData = activityLog.getOldData();
        dto.newData = activityLog.getNewData();
        dto.metadata = activityLog.getMetadata();
        dto.ipAddress = activityLog.getIpAddress();
        dto.userAgent = activityLog.getUserAgent();
        dto.createdAt = activityLog.getCreatedAt();
        return dto;
    }
    
    @PrePersist
    protected void onCreate() {
        if (logId == null) {
            logId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum EntityType {
        USER,
        TRANSACTION,
        TRANSFER,
        GROUP_CYCLE,
        PAYER_DETAILS,
        PAYOUT_DETAILS,
        GROUP,
        PAYMENT_METHOD
    }
}