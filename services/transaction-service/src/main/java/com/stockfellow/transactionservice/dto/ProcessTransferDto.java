package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.Transfer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class ProcessTransferDto {
    
    @NotBlank(message = "Paystack transfer code is required")
    private String paystackTransferCode;
    
    private String paystackRecipientCode;
    
    @NotNull(message = "Status is required")
    private Transfer.TransferStatus status;
    
    private String failureReason;

    // Constructors
    public ProcessTransferDto() {}

    // Getters and Setters
    public String getPaystackTransferCode() { return paystackTransferCode; }
    public void setPaystackTransferCode(String paystackTransferCode) { this.paystackTransferCode = paystackTransferCode; }
    
    public String getPaystackRecipientCode() { return paystackRecipientCode; }
    public void setPaystackRecipientCode(String paystackRecipientCode) { this.paystackRecipientCode = paystackRecipientCode; }
    
    public Transfer.TransferStatus getStatus() { return status; }
    public void setStatus(Transfer.TransferStatus status) { this.status = status; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}