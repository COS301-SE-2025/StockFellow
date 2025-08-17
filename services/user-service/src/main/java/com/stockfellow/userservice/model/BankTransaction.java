package com.stockfellow.userservice.model;

import java.time.LocalDate;

public class BankTransaction {
    private LocalDate date;
    private String description;
    private double amount;
    private double balance;
    private String category;
    private String reference;
    private String type; // DEBIT, CREDIT
    
    public BankTransaction() {}
    
    public BankTransaction(LocalDate date, String description, double amount, double balance) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.balance = balance;
    }
    
    // Getters and Setters
    public LocalDate getDate() { 
        return date; 
    }
    
    public void setDate(LocalDate date) { 
        this.date = date; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public double getAmount() { 
        return amount; 
    }
    
    public void setAmount(double amount) { 
        this.amount = amount; 
    }
    
    public double getBalance() { 
        return balance; 
    }
    
    public void setBalance(double balance) { 
        this.balance = balance; 
    }
    
    public String getCategory() { 
        return category; 
    }
    
    public void setCategory(String category) { 
        this.category = category; 
    }
    
    public String getReference() { 
        return reference; 
    }
    
    public void setReference(String reference) { 
        this.reference = reference; 
    }
    
    public String getType() { 
        return type; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }
}