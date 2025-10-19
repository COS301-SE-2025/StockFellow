package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
public class ProcessTransactionDto {
    
    @NotNull(message = "Status is required")
    private Transaction.TransactionStatus status;
    
    private String failureReason;

    // Constructors
    public ProcessTransactionDto() {}

    // Getters and Setters
    public Transaction.TransactionStatus getStatus() { return status; }
    public void setStatus(Transaction.TransactionStatus status) { this.status = status; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
