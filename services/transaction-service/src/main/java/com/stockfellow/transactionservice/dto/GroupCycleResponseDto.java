package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.GroupCycle;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class GroupCycleResponseDto {
    
    private UUID cycleId;
    private UUID groupId;
    private String cyclePeriod;
    private UUID recipientUserId;
    private BigDecimal contributionAmount;
    private BigDecimal currentTotal;
    private BigDecimal expectedTotal;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate collectionStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate collectionEndDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payoutDate;
    
    private Integer successfulCount;
    private Integer failedCount;
    private Integer pendingCount;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Static factory method
    public static GroupCycleResponseDto fromEntity(GroupCycle cycle) {
        GroupCycleResponseDto dto = new GroupCycleResponseDto();
        dto.cycleId = cycle.getCycleId();
        dto.groupId = cycle.getGroupId();
        dto.cyclePeriod = cycle.getCyclePeriod();
        dto.recipientUserId = cycle.getRecipientUserId();
        dto.contributionAmount = cycle.getContributionAmount();
        dto.currentTotal = cycle.getCurrentTotal();
        dto.expectedTotal = cycle.getExpectedTotal();
        dto.collectionStartDate = cycle.getCollectionStartDate();
        dto.collectionEndDate = cycle.getCollectionEndDate();
        dto.payoutDate = cycle.getPayoutDate();
        dto.successfulCount = cycle.getSuccessfulCount();
        dto.failedCount = cycle.getFailedCount();
        dto.pendingCount = cycle.getPendingCount();
        dto.status = cycle.getStatus();
        dto.createdAt = cycle.getCreatedAt();
        dto.updatedAt = cycle.getUpdatedAt();
        return dto;
    }

    // Getters and Setters
    public UUID getCycleId() { return cycleId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    
    public String getCyclePeriod() { return cyclePeriod; }
    public void setCyclePeriod(String cyclePeriod) { this.cyclePeriod = cyclePeriod; }
    
    public UUID getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
    
    public BigDecimal getCurrentTotal() { return currentTotal; }
    public void setCurrentTotal(BigDecimal currentTotal) { this.currentTotal = currentTotal; }
    
    public BigDecimal getExpectedTotal() { return expectedTotal; }
    public void setExpectedTotal(BigDecimal expectedTotal) { this.expectedTotal = expectedTotal; }
    
    public LocalDate getCollectionStartDate() { return collectionStartDate; }
    public void setCollectionStartDate(LocalDate collectionStartDate) { this.collectionStartDate = collectionStartDate; }
    
    public LocalDate getCollectionEndDate() { return collectionEndDate; }
    public void setCollectionEndDate(LocalDate collectionEndDate) { this.collectionEndDate = collectionEndDate; }
    
    public LocalDate getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
    
    public Integer getSuccessfulCount() { return successfulCount; }
    public void setSuccessfulCount(Integer successfulCount) { this.successfulCount = successfulCount; }
    
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    
    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}