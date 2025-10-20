package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {
    
    @Id
    @Column(name = "transfer_id")
    private UUID transferId;
    
    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "payout_detail_id", nullable = false)
    private UUID payoutDetailId;
    
    @Column(name = "paystack_transfer_code")
    private String paystackTransferCode;
    
    @Column(name = "paystack_recipient_code")
    private String paystackRecipientCode;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency")
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

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

    // Constructors
    public Transfer() {}

    public Transfer(UUID cycleId, UUID userId, UUID payoutDetailId, BigDecimal amount, String currency) {
        this.cycleId = cycleId;
        this.userId = userId;
        this.payoutDetailId = payoutDetailId;
        this.amount = amount;
        this.currency = currency;
        this.status = TransferStatus.PENDING;
    }

    // Getters
    public UUID getTransferId() { return transferId; }
    public UUID getCycleId() { return cycleId; }
    public UUID getUserId() { return userId; }
    public UUID getPayoutDetailId() { return payoutDetailId; }
    public String getPaystackTransferCode() { return paystackTransferCode; }
    public String getPaystackRecipientCode() { return paystackRecipientCode; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransferStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Integer getRetryCount() { return retryCount; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setTransferId(UUID transferId) { this.transferId = transferId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setPayoutDetailId(UUID payoutDetailId) { this.payoutDetailId = payoutDetailId; }
    public void setPaystackTransferCode(String paystackTransferCode) { this.paystackTransferCode = paystackTransferCode; }
    public void setPaystackRecipientCode(String paystackRecipientCode) { this.paystackRecipientCode = paystackRecipientCode; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setStatus(TransferStatus status) { this.status = status; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (transferId == null) {
            transferId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (status == null) {
            status = TransferStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransferStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REVERSED
    }
}

