package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import jakarta.validation.constraints.*;

public class ProcessTransferDto {
    
    @NotBlank(message = "Paystack transfer code is required")
    private String paystackTransferCode;
    
    private String paystackRecipientCode;
    
    @NotNull(message = "Status is required")
    private Transfer.TransferStatus status;
    
    private String gatewayStatus;
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
    
    public String getGatewayStatus() { return gatewayStatus; }
    public void setGatewayStatus(String gatewayStatus) { this.gatewayStatus = gatewayStatus; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}