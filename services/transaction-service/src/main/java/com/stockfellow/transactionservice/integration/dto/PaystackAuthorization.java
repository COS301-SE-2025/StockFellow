package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Paystack Authorization object (card details)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackAuthorization {
    
    @JsonProperty("authorization_code")
    private String authorizationCode;
    
    @JsonProperty("bin")
    private String bin;
    
    @JsonProperty("last4")
    private String last4;
    
    @JsonProperty("exp_month")
    private String expMonth;
    
    @JsonProperty("exp_year")
    private String expYear;
    
    @JsonProperty("channel")
    private String channel;
    
    @JsonProperty("card_type")
    private String cardType;
    
    @JsonProperty("bank")
    private String bank;
    
    @JsonProperty("country_code")
    private String countryCode;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("reusable")
    private Boolean reusable;
    
    @JsonProperty("signature")
    private String signature;

    // Getters and Setters
    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }
    
    public String getBin() { return bin; }
    public void setBin(String bin) { this.bin = bin; }
    
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    
    public String getExpMonth() { return expMonth; }
    public void setExpMonth(String expMonth) { this.expMonth = expMonth; }
    
    public String getExpYear() { return expYear; }
    public void setExpYear(String expYear) { this.expYear = expYear; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    
    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public Boolean getReusable() { return reusable; }
    public void setReusable(Boolean reusable) { this.reusable = reusable; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
