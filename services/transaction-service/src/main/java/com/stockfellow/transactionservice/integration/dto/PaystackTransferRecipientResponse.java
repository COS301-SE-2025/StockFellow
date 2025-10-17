package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

/*
 * Transfer Recipient (Bank details for payout)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransferRecipientResponse {

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private PaystackTransferRecipientData data;

    public PaystackTransferRecipientResponse() {}

    public Boolean getStatus(){ return status; }
    public String getMessage(){ return message; }
    public PaystackTransferRecipientData getData(){ return data; }

    public void setStatus(Boolean status){ this.status=status; }
    public void setMessage(String message){ this.message=message; }
    public void setData(PaystackTransferRecipientData data){ this.data=data; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaystackTransferRecipientData {
        
        @JsonProperty("active")
        private Boolean active;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("domain")
        private String domain;

        @JsonProperty("id")
        private Long id;

        @JsonProperty("integration")
        private Long integration;

        @JsonProperty("name")
        private String name;

        @JsonProperty("recipient_code")
        private String recipientCode;

        @JsonProperty("type")
        private String type;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("is_deleted")
        private Boolean isDeleted;

        @JsonProperty("details")
        private PaystackTransferRecipient details;

        public PaystackTransferRecipientData() {}

        public Boolean getActive(){ return active; }
        public String getCreatedAt(){ return createdAt; }
        public String getCurrency(){ return currency; }
        public String getDomain(){ return domain; }
        public Long getId(){ return id; }
        public Long getIntegration(){ return integration; }
        public String getName(){ return name; }
        public String getRecipientCode(){ return recipientCode; }
        public String getType(){ return type; }
        public String getUpdatedAt(){ return updatedAt; }
        public Boolean getIsDeleted(){ return isDeleted; }
        public PaystackTransferRecipient getDetails(){ return details; }

        public void setActive(Boolean active){ this.active=active; }
        public void setCreatedAt(String createdAt){ this.createdAt=createdAt; }
        public void setCurrency(String currency){ this.currency=currency; }
        public void setDomain(String domain){ this.domain=domain; }
        public void setId(Long id){ this.id=id; }
        public void setIntegration(Long integration){this.integration=integration; }
        public void setName(String name){ this.name=name; }
        public void setRecipientCode(String recipientCode){ this.recipientCode=recipientCode; }
        public void setType(String type){ this.type=type; }
        public void setUpdatedAt(String updatedAt){ this.updatedAt=updatedAt; }
        public void setIsDeleted(Boolean isDeleted){ this.isDeleted=isDeleted; }
        public void setDetails(PaystackTransferRecipient details){ this.details=details; }
    }
}
