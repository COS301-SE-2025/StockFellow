package com.stockfellow.transactionservice.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePayoutDetailsDto {
    
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")  // Add this
    private UUID userId;
    
    @NotNull(message = "Payout type is required")
    private String type;
    
    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    @JsonProperty("name")  // Add this
    private String recipientName;
    
    @NotBlank(message = "Account number is required")
    @JsonProperty("account_number")  // Add this
    private String accountNumber;
    
    @NotBlank(message = "Bank code is required")
    @JsonProperty("bank_code")  // Add this
    private String bankCode;
    
    @NotBlank(message = "Bank name is required")
    @JsonProperty("bank_name")  // Add this
    private String bankName;
    
    // Optional fields
    private String phoneNumber;
    private String provider;
    private String recipientCode;

    // Constructors
    public CreatePayoutDetailsDto() {}

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getRecipientCode() { return recipientCode; }
    public void setRecipientCode(String recipientCode) { this.recipientCode = recipientCode; }
}