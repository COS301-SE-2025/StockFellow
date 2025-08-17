package com.stockfellow.userservice.dto;

import com.stockfellow.userservice.model.BankTransaction;
import java.util.List;

public class BankStatementUploadRequest {
    private List<BankTransaction> transactions;
    private String analysisType; // "full", "quick", "update"
    private String bankName;
    private String accountType;
    
    public BankStatementUploadRequest() {}
    
    // Getters and Setters
    public List<BankTransaction> getTransactions() { 
        return transactions; 
    }
    
    public void setTransactions(List<BankTransaction> transactions) { 
        this.transactions = transactions; 
    }
    
    public String getAnalysisType() { 
        return analysisType; 
    }
    
    public void setAnalysisType(String analysisType) { 
        this.analysisType = analysisType; 
    }
    
    public String getBankName() { 
        return bankName; 
    }
    
    public void setBankName(String bankName) { 
        this.bankName = bankName; 
    }
    
    public String getAccountType() { 
        return accountType; 
    }
    
    public void setAccountType(String accountType) { 
        this.accountType = accountType; 
    }
}