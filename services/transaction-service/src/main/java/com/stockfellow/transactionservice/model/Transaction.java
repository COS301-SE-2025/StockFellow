package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @Column(name = "trans_id")
    private UUID transactionId;
    
    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "payer_id", nullable = false)
    private UUID payerId;
    
    @Column(name = "paystack_reference")
    private String paystackReference;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Transaction() {}

    public Transaction(UUID cycleId, UUID userId, UUID payerId, BigDecimal amount, TransactionStatus status) {
        this.cycleId = cycleId;
        this.userId = userId;
        this.payerId = payerId;
        this.amount = amount;
        this.status = status;
    }

    // Getters
    public UUID getTransactionId() { return transactionId; }
    public UUID getCycleId() { return cycleId; }
    public UUID getUserId() { return userId; }
    public UUID getPayerId() { return payerId; }
    public String getPaystackReference() { return paystackReference; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Integer getRetryCount() { return retryCount; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setPayerId(UUID payerId) { this.payerId = payerId; }
    public void setPaystackReference(String paystackReference) { this.paystackReference = paystackReference; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (transactionId == null) {
            transactionId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PAUSED //Card is being challenged
    }
}