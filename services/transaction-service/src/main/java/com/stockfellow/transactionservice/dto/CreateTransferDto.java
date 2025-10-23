package com.stockfellow.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateTransferDto {
    
    @NotNull(message = "Cycle ID is required")
    private UUID cycleId;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Payout detail ID is required")
    private UUID payoutDetailId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;
    
    private String currency = "ZAR";

    @NotEmpty(message = "Reason for payment is required")
    private String reason; 

    // Constructors
    public CreateTransferDto() {}

    // Getters and Setters
    public UUID getCycleId() { return cycleId; }
    public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getPayoutDetailId() { return payoutDetailId; }
    public void setPayoutDetailId(UUID payoutDetailId) { this.payoutDetailId = payoutDetailId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCurrency(){ return currency; }
    //No set needed
}
