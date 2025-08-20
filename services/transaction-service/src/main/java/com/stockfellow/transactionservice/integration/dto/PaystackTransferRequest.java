package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransferRequest {
    
    @JsonProperty("source")
    private String source = "balance"; // "balance" or "bank"
    
    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Integer amount; 
    
    @JsonProperty("recipient")
    @NotBlank(message = "Recipient code is required")
    private String recipient; // Recipient code from create recipient API
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("currency")
    private String currency = "ZAR";
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Constructors
    public PaystackTransferRequest() {}

    public PaystackTransferRequest(Integer amount, String recipient, String reason) {
        this.amount = amount;
        this.recipient = recipient;
        this.reason = reason;
    }

    // Getters and Setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
