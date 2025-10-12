package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.PayerDetails;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public class PayerDetailsResponseDto {

    private UUID payerId;
    private UUID userId;
    private String type;
    private String authCode;
    private String cardType;
    private String last4;
    private String expMonth;
    private String expYear;
    private String bin;
    private String bank;
    private String email;
    private Boolean isAuthenticated;
    private String signature;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Static factory method
    public static PayerDetailsResponseDto fromEntity(PayerDetails payerDetails) {
        PayerDetailsResponseDto dto = new PayerDetailsResponseDto();
        dto.payerId = payerDetails.getPayerId();
        dto.userId = payerDetails.getUserId();
        dto.type = payerDetails.getType();
        dto.authCode = payerDetails.getAuthCode();
        dto.cardType = payerDetails.getCardType();
        dto.last4 = payerDetails.getLast4();
        dto.expMonth = payerDetails.getExpMonth();
        dto.expYear = payerDetails.getExpYear();
        dto.bin = payerDetails.getBin();
        dto.bank = payerDetails.getBank();
        dto.email = payerDetails.getEmail();
        dto.isAuthenticated = payerDetails.getIsAuthenticated();
        dto.signature = payerDetails.getSignature();
        dto.isActive = payerDetails.getIsActive();
        dto.createdAt = payerDetails.getCreatedAt();
        dto.updatedAt = payerDetails.getUpdatedAt();
        return dto;
    }

    // Getters and Setters
    public UUID getPayerId() { return payerId; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getAuthCode() { return authCode; }
    public String getCardType() { return cardType; }
    public String getLast4() { return last4; }
    public String getExpMonth() { return expMonth; }
    public String getExpYear() { return expYear; }
    public String getBin() { return bin; }
    public String getBank() { return bank; }
    public String getEmail() { return email; }
    public Boolean getIsAuthenticated() { return isAuthenticated; }
    public String getSignature() { return signature; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setPayerId(UUID payerId) { this.payerId = payerId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setAuthCode(String authCode) { this.authCode = authCode; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setLast4(String last4) { this.last4 = last4; }
    public void setExpMonth(String expMonth) { this.expMonth = expMonth; }
    public void setExpYear(String expYear) { this.expYear = expYear; }
    public void setBin(String bin) { this.bin = bin; }
    public void setBank(String bank) { this.bank = bank; }
    public void setEmail(String email) { this.email = email; }
    public void setIsAuthenticated(Boolean isAuthenticated) { this.isAuthenticated = isAuthenticated; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
