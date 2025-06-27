package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.GroupCycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CycleResponse {

    private UUID cycleId;    
    private UUID groupId;    
    private String cycleMonth;    
    private UUID recipientUserId;    
    private UUID recipientPaymentMethodId;    
    private BigDecimal contributionAmount;    
    private LocalDate collectionDate;    
    private BigDecimal totalExpectedAmount;    
    private Integer successfulPayments;
    private Integer failedPayments;
    private String status;    
    private LocalDateTime createdAt;    
    private LocalDateTime updatedAt;

    // Constructors
    public CycleResponse() {}

    public CycleResponse(UUID cycleId, UUID groupId, String cycleMonth, UUID recipientUserId, 
                        UUID recipientPaymentMethodId, BigDecimal contributionAmount, 
                        LocalDate collectionDate, BigDecimal totalExpectedAmount, 
                        Integer successfulPayments, Integer failedPayments, String status, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cycleId = cycleId;
        this.groupId = groupId;
        this.cycleMonth = cycleMonth;
        this.recipientUserId = recipientUserId;
        this.recipientPaymentMethodId = recipientPaymentMethodId;
        this.contributionAmount = contributionAmount;
        this.collectionDate = collectionDate;
        this.totalExpectedAmount = totalExpectedAmount;
        this.successfulPayments = successfulPayments;
        this.failedPayments = failedPayments;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getCycleId() { return cycleId; }
    public UUID getGroupId() { return groupId; }
    public String getCycleMonth() { return cycleMonth; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public UUID getRecipientPaymentMethodId() { return recipientPaymentMethodId; }
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public BigDecimal getTotalExpectedAmount() { return totalExpectedAmount; }
    public Integer getSuccessfulPayments() { return successfulPayments; }
    public Integer getFailedPayments() { return failedPayments; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public void setCycleMonth(String cycleMonth) { this.cycleMonth = cycleMonth; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    public void setRecipientPaymentMethodId(UUID recipientPaymentMethodId) { this.recipientPaymentMethodId = recipientPaymentMethodId; }
    public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public void setTotalExpectedAmount(BigDecimal totalExpectedAmount) { this.totalExpectedAmount = totalExpectedAmount; }
    public void setSuccessfulPayments(Integer successfulPayments) { this.successfulPayments = successfulPayments; }
    public void setFailedPayments(Integer failedPayments) { this.failedPayments = failedPayments; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Factory method to create CycleResponse from GroupCycle entity
    public static CycleResponse from(GroupCycle cycle) {
        return new CycleResponse(
            cycle.getCycleId(),
            cycle.getGroupId(),
            cycle.getCycleMonth(),
            cycle.getRecipientUserId(),
            cycle.getRecipientPaymentMethodId(),
            cycle.getContributionAmount(),
            cycle.getCollectionDate(),
            cycle.getTotalExpectedAmount(),
            cycle.getSuccessfulPayments(),
            cycle.getFailedPayments(),
            cycle.getStatus(),
            cycle.getCreatedAt(),
            cycle.getUpdatedAt()
        );
    }
}