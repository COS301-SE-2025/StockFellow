package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for initiating a Paystack transaction (charge)
 * Used for collecting money from users
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransactionRequest {
    
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Integer amount; // Amount in cents
    
    @JsonProperty("currency")
    private String currency = "ZAR";
    
    @JsonProperty("reference")
    private String reference; // Unique transaction reference
    
    @JsonProperty("callback_url")
    private String callbackUrl;
    
    @JsonProperty("plan")
    private String plan;
    
    @JsonProperty("invoice_limit")
    private Integer invoiceLimit;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("channels")
    private List<String> channels; // ["card", "bank", "ussd", "qr", "mobile_money"]
    
    @JsonProperty("split_code")
    private String splitCode;
    
    @JsonProperty("subaccount")
    private String subaccount;
    
    @JsonProperty("transaction_charge")
    private Integer transactionCharge;
    
    @JsonProperty("bearer")
    private String bearer; // "account" or "subaccount"

    // Constructors
    public PaystackTransactionRequest() {}

    public PaystackTransactionRequest(String email, Integer amount, String reference) {
        this.email = email;
        this.amount = amount;
        this.reference = reference;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
    
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    
    public Integer getInvoiceLimit() { return invoiceLimit; }
    public void setInvoiceLimit(Integer invoiceLimit) { this.invoiceLimit = invoiceLimit; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }
    
    public String getSplitCode() { return splitCode; }
    public void setSplitCode(String splitCode) { this.splitCode = splitCode; }
    
    public String getSubaccount() { return subaccount; }
    public void setSubaccount(String subaccount) { this.subaccount = subaccount; }
    
    public Integer getTransactionCharge() { return transactionCharge; }
    public void setTransactionCharge(Integer transactionCharge) { this.transactionCharge = transactionCharge; }
    
    public String getBearer() { return bearer; }
    public void setBearer(String bearer) { this.bearer = bearer; }
}

