package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackChargeRequest {
        
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Integer amount; // Amount in cents
    
    @JsonProperty("authorization_code")
    @NotNull(message = "Amount is required")
    private String authCode;

    public PaystackChargeRequest() {}
    
    public PaystackChargeRequest(String email, Integer amount, String authCode) {
        this.email = email;
        this.amount = amount;
        this.authCode = authCode;
    }

    public String getEmail(){ return email; }
    public Integer getAmount(){ return amount; }
    public String getAuthCode(){ return authCode; }

    public void setEmail(String email){ this.email = email; }
    public void setAmount(Integer amount){ this.amount = amount; }
    public void setAuthCode(String authCode){ this.authCode = authCode; }
}
