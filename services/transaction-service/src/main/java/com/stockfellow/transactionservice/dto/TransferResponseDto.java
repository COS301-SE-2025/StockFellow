package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransferResponseDto {
    
    private UUID transferId;
    private UUID cycleId;
    private UUID userId;
    private UUID payoutDetailId;
    private String paystackTransferCode;
    private String paystackRecipientCode;
    private BigDecimal amount;
    private String currency;
    private Transfer.TransferStatus status;
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
    public static TransferResponseDto fromEntity(Transfer transfer) {
        TransferResponseDto dto = new TransferResponseDto();
        dto.transferId = transfer.getTransferId();
        dto.cycleId = transfer.getCycleId();
        dto.userId = transfer.getUserId();
        dto.payoutDetailId = transfer.getPayoutDetailId();
        dto.paystackTransferCode = transfer.getPaystackTransferCode();
        dto.paystackRecipientCode = transfer.getPaystackRecipientCode();
        dto.amount = transfer.getAmount();
        dto.currency = transfer.getCurrency();
        dto.status = transfer.getStatus();
        dto.gatewayStatus = transfer.getGatewayStatus();
        dto.failureReason = transfer.getFailureReason();
        dto.retryCount = transfer.getRetryCount();
        dto.initiatedAt = transfer.getInitiatedAt();
        dto.completedAt = transfer.getCompletedAt();
        dto.createdAt = transfer.getCreatedAt();
        dto.updatedAt = transfer.getUpdatedAt();
        return dto;
    }

    // Getters and Setters
    public UUID getTransferId() { return transferId; }
    public void setTransferId(UUID transferId) { this.transferId = transferId; }
    
    public UUID getCycleId() { return cycleId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getPayoutDetailId() { return payoutDetailId; }
    public void setPayoutDetailId(UUID payoutDetailId) { this.payoutDetailId = payoutDetailId; }
    
    public String getPaystackTransferCode() { return paystackTransferCode; }
    public void setPaystackTransferCode(String paystackTransferCode) { this.paystackTransferCode = paystackTransferCode; }
    
    public String getPaystackRecipientCode() { return paystackRecipientCode; }
    public void setPaystackRecipientCode(String paystackRecipientCode) { this.paystackRecipientCode = paystackRecipientCode; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Transfer.TransferStatus getStatus() { return status; }
    public void setStatus(Transfer.TransferStatus status) { this.status = status; }
    
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