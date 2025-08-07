package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;

public class UpdatePayoutDetailsDto {
    
    private PayoutDetails.PayoutType type;
    private String recipientName;
    private String accountNumber;
    private String bankCode;
    private String bankName;
    private String phoneNumber;
    private String provider;
    private String recipientCode;
    private Boolean isVerified;

    // Constructors
    public UpdatePayoutDetailsDto() {}

    // Getters and Setters
    public PayoutDetails.PayoutType getType() { return type; }
    public void setType(PayoutDetails.PayoutType type) { this.type = type; }
    
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
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
}