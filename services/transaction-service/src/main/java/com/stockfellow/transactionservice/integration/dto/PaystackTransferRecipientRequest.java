package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransferRecipientRequest {
    @JsonProperty("type")
    private String type;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("currency")
    private String currency;

    public PaystackTransferRecipientRequest() {}

    // Getters
    public String getType() {return type;}
    public String getAccountNumber() {return accountNumber;}
    public String getName() {return name;}
    public String getBankCode() {return bankCode;}
    public String getCurrency() {return currency;}

    // Setters
    public void setType(String type) {this.type = type;}
    public void setAccountNumber(String accountNumber) {this.accountNumber = accountNumber;}
    public void setName(String accountName) {this.name = accountName;}
    public void setBankCode(String bankCode) {this.bankCode = bankCode;}
    public void setCurrency(String currency) { this.currency = currency;}

    @Override
    public String toString() {
        return "PaystackTransferRecipientRequest{" +
                "type='" + type + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountName='" + name + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
