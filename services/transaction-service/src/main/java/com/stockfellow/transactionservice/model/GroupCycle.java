package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_cycles")
public class GroupCycle {
    
    @Id
    @Column(name = "cycle_id")
    private UUID cycleId;
    
    @Column(name = "group_id", nullable = false)
    private UUID groupId;
    
    @Column(name = "cycle_month", nullable = false, length = 7)
    private String cycleMonth;
    
    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;
    
    @Column(name = "recipient_payment_method_id", nullable = false)
    private UUID recipientPaymentMethodId;
    
    @Column(name = "contribution_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal contributionAmount;
    
    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;
    
    @Column(name = "total_expected_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpectedAmount;
    
    @Column(name = "successful_payments", nullable = false)
    private Integer successfulPayments = 0;
    
    @Column(name = "failed_payments", nullable = false)
    private Integer failedPayments = 0;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

    @PrePersist
    protected void onCreate() {
        if (cycleId == null) {
            cycleId = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (successfulPayments == null) {
            successfulPayments = 0;
        }
        if (failedPayments == null) {
            failedPayments = 0;
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}