package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

public class CreatePayerDetailsDto {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Payment method type is required")
    private String type;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String authCode;
    private String cardType;
    private String last4;
    private String expMonth;
    private String expYear;
    private String bin;
    private String bank;
    private String signature;

    // Constructors
    public CreatePayerDetailsDto() {}

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAuthCode() { return authCode; }
    public void setAuthCode(String authCode) { this.authCode = authCode; }
    
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    
    public String getExpMonth() { return expMonth; }
    public void setExpMonth(String expMonth) { this.expMonth = expMonth; }
    
    public String getExpYear() { return expYear; }
    public void setExpYear(String expYear) { this.expYear = expYear; }
    
    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getBin() { return bin; }
    public void setBin(String bin) { this.bin = bin; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}