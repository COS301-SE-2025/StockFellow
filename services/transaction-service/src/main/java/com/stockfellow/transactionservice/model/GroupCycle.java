package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_cycle")
public class GroupCycle {
    
    @Id
    @Column(name = "cycle_id")
    private UUID cycleId;
    
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "rotation_id", nullable = false)
    private UUID rotationId;
    
    @Column(name = "cycle_period", nullable = false)
    private String cyclePeriod;
    
    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;
    
    @Column(name = "contribution_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal contributionAmount;
    
    @Column(name = "current_total", precision = 19, scale = 2)
    private BigDecimal currentTotal;
    
    @Column(name = "expected_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedTotal;
    
    @Column(name = "collection_start_date", nullable = false)
    private LocalDate collectionStartDate;
    
    @Column(name = "collection_end_date", nullable = false)
    private LocalDate collectionEndDate;
    
    @Column(name = "payout_date")
    private LocalDate payoutDate;
    
    @Column(name = "successful_count")
    private Integer successfulCount = 0;
    
    @Column(name = "failed_count")
    private Integer failedCount = 0;
    
    @Column(name = "pending_count")
    private Integer pendingCount = 0;
    
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "member_ids")
    private UUID[] memberIds;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    

    // Constructors
    public GroupCycle() {}

    public GroupCycle(String groupId, UUID rotationId, String cyclePeriod, UUID recipientUserId, 
                     BigDecimal contributionAmount, BigDecimal expectedTotal,
                     LocalDate collectionStartDate, LocalDate collectionEndDate, LocalDate payoutDate, UUID[] memberIds) {
        this.groupId = groupId;
        this.rotationId = rotationId;
        this.cyclePeriod = cyclePeriod;
        this.recipientUserId = recipientUserId;
        this.contributionAmount = contributionAmount;
        this.expectedTotal = expectedTotal;
        this.collectionStartDate = collectionStartDate;
        this.collectionEndDate = collectionEndDate;
        this.status = "PENDING";
        this.memberIds = memberIds;
        this.currentTotal = BigDecimal.ZERO;
        this.payoutDate = payoutDate;
        
    }

    // Getters
    public UUID getCycleId() { return cycleId; }
    public String getGroupId() { return groupId; }
    public UUID getRotationId() { return rotationId; }    
    public String getCyclePeriod() { return cyclePeriod; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public BigDecimal getCurrentTotal() { return currentTotal; }
    public BigDecimal getExpectedTotal() { return expectedTotal; }
    public LocalDate getCollectionStartDate() { return collectionStartDate; }
    public LocalDate getCollectionEndDate() { return collectionEndDate; }
    public LocalDate getPayoutDate() { return payoutDate; }
    public Integer getSuccessfulCount() { return successfulCount; }
    public Integer getFailedCount() { return failedCount; }
    public Integer getPendingCount() { return pendingCount; }
    public String getStatus() { return status; }
    public UUID[] getMemberIds() { return memberIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setRotationId(UUID rotationId) { this.rotationId = rotationId; }    
    public void setCyclePeriod(String cyclePeriod) { this.cyclePeriod = cyclePeriod; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
    public void setCurrentTotal(BigDecimal currentTotal) { this.currentTotal = currentTotal; }
    public void setExpectedTotal(BigDecimal expectedTotal) { this.expectedTotal = expectedTotal; }
    public void setCollectionStartDate(LocalDate collectionStartDate) { this.collectionStartDate = collectionStartDate; }
    public void setCollectionEndDate(LocalDate collectionEndDate) { this.collectionEndDate = collectionEndDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
    public void setSuccessfulCount(Integer successfulCount) { this.successfulCount = successfulCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
    public void setStatus(String status) { this.status = status; }
    public void setMemberIds(UUID[] memberIds) { this.memberIds = memberIds; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (cycleId == null) {
            cycleId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (currentTotal == null) {
            currentTotal = BigDecimal.ZERO;
        }
        if (successfulCount == null) successfulCount = 0;
        if (failedCount == null) failedCount = 0;
        if (pendingCount == null) pendingCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // // Cycle Status Enum
    // public enum CycleStatus {
    //     PENDING,
    //     ACTIVE,
    //     COLLECTING,
    //     COLLECTION_COMPLETE,
    //     PAYOUT_PENDING,
    //     COMPLETED,
    //     FAILED,
    //     CANCELLED
    // }
}