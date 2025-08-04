// package com.stockfellow.transactionservice.dto;

// import com.stockfellow.transactionservice.model.*;
// import com.fasterxml.jackson.annotation.JsonFormat;
// import com.fasterxml.jackson.databind.JsonNode;
// import jakarta.validation.constraints.*;
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.UUID;

// public class ActivityLogResponseDto {
    
//     private UUID logId;
//     private UUID userId;
//     private UUID cycleId;
//     private ActivityLog.EntityType entityType;
//     private UUID entityId;
//     private String action;
//     private JsonNode oldData;
//     private JsonNode newData;
//     private JsonNode metadata;
//     private String ipAddress;
//     private String userAgent;
    
//     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//     private LocalDateTime createdAt;

//     // Static factory method
//     public static ActivityLogResponseDto fromEntity(ActivityLog activityLog) {
//         ActivityLogResponseDto dto = new ActivityLogResponseDto();
//         dto.logId = activityLog.getLogId();
//         dto.userId = activityLog.getUserId();
//         dto.cycleId = activityLog.getCycleId();
//         dto.entityType = activityLog.getEntityType();
//         dto.entityId = activityLog.getEntityId();
//         dto.action = activityLog.getAction();
//         dto.oldData = activityLog.getOldData();
//         dto.newData = activityLog.getNewData();
//         dto.metadata = activityLog.getMetadata();
//         dto.ipAddress = activityLog.getIpAddress();
//         dto.userAgent = activityLog.getUserAgent();
//         dto.createdAt = activityLog.getCreatedAt();
//         return dto;
//     }

//     // Getters and Setters
//     public UUID getLogId() { return logId; }
//     public void setLogId(UUID logId) { this.logId = logId; }
    
//     public UUID getUserId() { return userId; }
//     public void setUserId(UUID userId) { this.userId = userId; }
    
//     public UUID getCycleId() { return cycleId; }
//     public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    
//     public ActivityLog.EntityType getEntityType() { return entityType; }
//     public void setEntityType(ActivityLog.EntityType entityType) { this.entityType = entityType; }
    
//     public UUID getEntityId() { return entityId; }
//     public void setEntityId(UUID entityId) { this.entityId = entityId; }
    
//     public String getAction() { return action; }
//     public void setAction(String action) { this.action = action; }
    
//     public JsonNode getOldData() { return oldData; }
//     public void setOldData(JsonNode oldData) { this.oldData = oldData; }
    
//     public JsonNode getNewData() { return newData; }
//     public void setNewData(JsonNode newData) { this.newData = newData; }
    
//     public JsonNode getMetadata() { return metadata; }
//     public void setMetadata(JsonNode metadata) { this.metadata = metadata; }
    
//     public String getIpAddress() { return ipAddress; }
//     public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
//     public String getUserAgent() { return userAgent; }
//     public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
//     public LocalDateTime getCreatedAt() { return createdAt; }
//     public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
// }