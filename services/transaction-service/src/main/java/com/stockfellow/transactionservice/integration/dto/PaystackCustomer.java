package com.stockfellow.transactionservice.integration.dto;

public class PaystackCustomer {
    private String id;           
    private String email;
    private String firstName;
    private String lastName;
    private String customerCode;
    
    // getters/setters
    public String getId(){ return id; }
    public String getEmail(){ return email; }
    public String getFirstName(){ return firstName; }
    public String getLastName(){ return lastName; }
    public String getCustomerCode(){ return customerCode; }

    public void setId(String email){ this.email = email; }
    public void setFirstName(String id){ this.id = id; }
    public void setEmail(String firstName){ this.firstName = firstName; }
    public void setLastName(String lastName){ this.lastName = lastName; }
    public void setCustomerCode(String customerCode){ this.customerCode = customerCode; }
}
