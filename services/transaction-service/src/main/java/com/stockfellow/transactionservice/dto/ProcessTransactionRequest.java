package com.stockfellow.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ProcessTransactionRequest {

    @NotNull(message = "Cycle ID is required")
    private UUID cycleId;

    // maybe for failed payments by that user?
    private UUID specificPayerUserId;

    // max retries for a failed payment?
    private Integer maxRetries = 3;

    // "retrying failed transaction"
    private String processingReason;

    // Constructors
    public ProcessTransactionRequest() {}

    public ProcessTransactionRequest(UUID cycleId, UUID specificPayerUserId, Integer maxRetries, String processingReason) {
        this.cycleId = cycleId;
        this.specificPayerUserId = specificPayerUserId;
        this.maxRetries = maxRetries != null ? maxRetries : 3;
        this.processingReason = processingReason;
    }

    // Getters
    public UUID getCycleId() { return cycleId; }
    public UUID getSpecificPayerUserId() { return specificPayerUserId; }
    public Integer getMaxRetries() { return maxRetries; }
    public String getProcessingReason() { return processingReason; }

    // Setters
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    public void setSpecificPayerUserId(UUID specificPayerUserId) { this.specificPayerUserId = specificPayerUserId; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries != null ? maxRetries : 3; }
    public void setProcessingReason(String processingReason) { this.processingReason = processingReason; }
}