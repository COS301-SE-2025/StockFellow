package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import jakarta.validation.constraints.*;

public class ProcessTransactionDto {
    
    @NotBlank(message = "Paystack transaction ID is required")
    private String paystackTransId;
    
    @NotNull(message = "Status is required")
    private Transaction.TransactionStatus status;
    
    private String gatewayStatus;
    private String failureReason;

    // Constructors
    public ProcessTransactionDto() {}

    // Getters and Setters
    public String getPaystackTransId() { return paystackTransId; }
    public void setPaystackTransId(String paystackTransId) { this.paystackTransId = paystackTransId; }
    
    public Transaction.TransactionStatus getStatus() { return status; }
    public void setStatus(Transaction.TransactionStatus status) { this.status = status; }
    
    public String getGatewayStatus() { return gatewayStatus; }
    public void setGatewayStatus(String gatewayStatus) { this.gatewayStatus = gatewayStatus; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
