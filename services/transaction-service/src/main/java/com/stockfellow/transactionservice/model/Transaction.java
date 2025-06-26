package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @Column(name = "transaction_id")
    private UUID transactionId;
    
    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;
    
    @Column(name = "mandate_id", nullable = false)
    private UUID mandateId;
    
    @Column(name = "payer_user_id", nullable = false)
    private UUID payerUserId;
    
    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;
    
    @Column(name = "group_id", nullable = false)
    private UUID groupId;
    
    @Column(name = "payer_payment_method_id", nullable = false)
    private UUID payerPaymentMethodId;
    
    @Column(name = "recipient_payment_method_id", nullable = false)
    private UUID recipientPaymentMethodId;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "external_reference")
    private String externalReference;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "fail_message")
    private String failMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Getters
    public UUID getTransactionId() { return transactionId; }
    public UUID getCycleId() { return cycleId; }
    public UUID getMandateId() { return mandateId; }
    public UUID getPayerUserId() { return payerUserId; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public UUID getGroupId() { return groupId; }
    public UUID getPayerPaymentMethodId() { return payerPaymentMethodId; }
    public UUID getRecipientPaymentMethodId() { return recipientPaymentMethodId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getExternalReference() { return externalReference; }
    public Integer getRetryCount() { return retryCount; }
    public String getFailMessage() { return failMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    // Setters
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setMandateId(UUID mandateId) { this.mandateId = mandateId; }
    public void setPayerUserId(UUID payerUserId) { this.payerUserId = payerUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public void setPayerPaymentMethodId(UUID payerPaymentMethodId) { this.payerPaymentMethodId = payerPaymentMethodId; }
    public void setRecipientPaymentMethodId(UUID recipientPaymentMethodId) { this.recipientPaymentMethodId = recipientPaymentMethodId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public void setFailMessage(String failMessage) { this.failMessage = failMessage; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

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
        // Update logic if needed
    }
}