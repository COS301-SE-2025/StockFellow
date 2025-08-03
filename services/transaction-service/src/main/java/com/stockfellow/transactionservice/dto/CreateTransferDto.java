package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    
    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency;

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
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
