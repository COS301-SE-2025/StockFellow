package com.stockfellow.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionResponse {

    private UUID transactionId;
    private UUID cycleId;
    private UUID mandateId;
    private UUID payerUserId;
    private UUID recipientUserId;
    private UUID groupId;
    private UUID payerPaymentMethodId;
    private UUID recipientPaymentMethodId;
    private BigDecimal amount;
    private String status;
    private String externalReference;
    private Integer retryCount;
    private String failMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // user-friendly fields
    private String statusDescription;
    private String payerName;
    private String recipientName;

    // Constructors
    public TransactionResponse() {}

    public TransactionResponse(UUID transactionId, UUID cycleId, UUID mandateId, UUID payerUserId, 
                              UUID recipientUserId, UUID groupId, UUID payerPaymentMethodId, 
                              UUID recipientPaymentMethodId, BigDecimal amount, String status, 
                              String externalReference, Integer retryCount, String failMessage, 
                              LocalDateTime createdAt, LocalDateTime completedAt, String statusDescription, 
                              String payerName, String recipientName) {
        this.transactionId = transactionId;
        this.cycleId = cycleId;
        this.mandateId = mandateId;
        this.payerUserId = payerUserId;
        this.recipientUserId = recipientUserId;
        this.groupId = groupId;
        this.payerPaymentMethodId = payerPaymentMethodId;
        this.recipientPaymentMethodId = recipientPaymentMethodId;
        this.amount = amount;
        this.status = status;
        this.externalReference = externalReference;
        this.retryCount = retryCount;
        this.failMessage = failMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.statusDescription = statusDescription;
        this.payerName = payerName;
        this.recipientName = recipientName;
    }

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
    public String getStatusDescription() { return statusDescription; }
    public String getPayerName() { return payerName; }
    public String getRecipientName() { return recipientName; }

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
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    public void setPayerName(String payerName) { this.payerName = payerName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public static TransactionResponse fromTransaction(
            com.stockfellow.transactionservice.model.Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getCycleId(),
                transaction.getMandateId(),
                transaction.getPayerUserId(),
                transaction.getRecipientUserId(),
                transaction.getGroupId(),
                transaction.getPayerPaymentMethodId(),
                transaction.getRecipientPaymentMethodId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getExternalReference(),
                transaction.getRetryCount(),
                transaction.getFailMessage(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt(),
                getStatusDescription(transaction.getStatus()),
                null, // payerName - can be set later if needed
                null  // recipientName - can be set later if needed
        );
    }

    private static String getStatusDescription(String status) {
        return switch (status) {
            case "PENDING" -> "Transaction is pending";
            case "PROCESSING" -> "Transaction is being processed";
            case "COMPLETED" -> "Transaction completed successfully";
            case "FAILED" -> "Transaction failed";
            default -> "Unknown status";
        };
    }
}