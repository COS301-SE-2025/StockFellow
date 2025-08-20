package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransferRecipient {

    @JsonProperty("authorization_code")
    public String authCode;

    @JsonProperty("account_number")
    public String accountNumber;

    @JsonProperty("account_name")
    public String accountName;

    @JsonProperty("bank_code")
    public String bankCode;

    @JsonProperty("bank_name")
    public String bankName;

    public PaystackTransferRecipient() {}

    // Getters
    public String getAuthCode() {return authCode;}
    public String getAccountNumber() {return accountNumber;}
    public String getAccountName() {return accountName;}
    public String getBankCode() {return bankCode;}
    public String getBankName() {return bankName;}

    // Setters
    public void setAuthCode(String authCode) {this.authCode = authCode;}
    public void setAccountNumber(String accountNumber) {this.accountNumber = accountNumber;}
    public void setAccountName(String accountName) {this.accountName = accountName;}
    public void setBankCode(String bankCode) {this.bankCode = bankCode;}
    public void setBankName(String bankName) { this.bankName = bankName;}

    @Override
    public String toString() {
        return "PaystackTransferRecipient{" +
                "authCode='" + authCode + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountName='" + accountName + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", bankName='" + bankName + '\'' +
                '}';
    }
}
