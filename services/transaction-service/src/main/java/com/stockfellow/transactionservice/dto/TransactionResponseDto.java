package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionResponseDto {
    
    private UUID transactionId;
    private UUID cycleId;
    private UUID userId;
    private UUID payerId;
    private String paystackReference;
    private String paystackTransId;
    private BigDecimal amount;
    private Transaction.TransactionStatus status;
    private String gatewayStatus;
    private String failureReason;
    private Integer retryCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime initiatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Static factory method
    public static TransactionResponseDto fromEntity(Transaction transaction) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.transactionId = transaction.getTransactionId();
        dto.cycleId = transaction.getCycleId();
        dto.userId = transaction.getUserId();
        dto.payerId = transaction.getPayerId();
        dto.paystackReference = transaction.getPaystackReference();
        dto.paystackTransId = transaction.getPaystackTransId();
        dto.amount = transaction.getAmount();
        dto.status = transaction.getStatus();
        dto.gatewayStatus = transaction.getGatewayStatus();
        dto.failureReason = transaction.getFailureReason();
        dto.retryCount = transaction.getRetryCount();
        dto.initiatedAt = transaction.getInitiatedAt();
        dto.completedAt = transaction.getCompletedAt();
        dto.createdAt = transaction.getCreatedAt();
        dto.updatedAt = transaction.getUpdatedAt();
        return dto;
    }

    // Getters and Setters
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    
    public UUID getCycleId() { return cycleId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getPayerId() { return payerId; }
    public void setPayerId(UUID payerId) { this.payerId = payerId; }
    
    public String getPaystackReference() { return paystackReference; }
    public void setPaystackReference(String paystackReference) { this.paystackReference = paystackReference; }
    
    public String getPaystackTransId() { return paystackTransId; }
    public void setPaystackTransId(String paystackTransId) { this.paystackTransId = paystackTransId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public Transaction.TransactionStatus getStatus() { return status; }
    public void setStatus(Transaction.TransactionStatus status) { this.status = status; }
    
    public String getGatewayStatus() { return gatewayStatus; }
    public void setGatewayStatus(String gatewayStatus) { this.gatewayStatus = gatewayStatus; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
