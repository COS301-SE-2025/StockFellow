package com.stockfellow.userservice.dto;

public class BankStatementAnalysis {
    private double averageMonthlyIncome;
    private double averageMonthlyExpenses;
    private double averageMonthlySavings;
    private double averageBalance;
    
    private double incomeStability; // Coefficient of variation
    private double expenseStability;
    private double savingsRate;
    private double expenseToIncomeRatio;
    
    private int overdraftCount;
    private int gamblingTransactions;
    private int investmentTransactions;
    
    private double incomeGrowthTrend;
    private int totalTransactionsAnalyzed;
    private int monthsAnalyzed;
    
    public BankStatementAnalysis() {}
    
    // Getters and Setters
    public double getAverageMonthlyIncome() { 
        return averageMonthlyIncome; 
    }
    
    public void setAverageMonthlyIncome(double averageMonthlyIncome) { 
        this.averageMonthlyIncome = averageMonthlyIncome; 
    }
    
    public double getAverageMonthlyExpenses() { 
        return averageMonthlyExpenses; 
    }
    
    public void setAverageMonthlyExpenses(double averageMonthlyExpenses) { 
        this.averageMonthlyExpenses = averageMonthlyExpenses; 
    }
    
    public double getAverageMonthlySavings() { 
        return averageMonthlySavings; 
    }
    
    public void setAverageMonthlySavings(double averageMonthlySavings) { 
        this.averageMonthlySavings = averageMonthlySavings; 
    }
    
    public double getAverageBalance() { 
        return averageBalance; 
    }
    
    public void setAverageBalance(double averageBalance) { 
        this.averageBalance = averageBalance; 
    }
    
    public double getIncomeStability() { 
        return incomeStability; 
    }
    
    public void setIncomeStability(double incomeStability) { 
        this.incomeStability = incomeStability; 
    }
    
    public double getExpenseStability() { 
        return expenseStability; 
    }
    
    public void setExpenseStability(double expenseStability) { 
        this.expenseStability = expenseStability; 
    }
    
    public double getSavingsRate() { 
        return savingsRate; 
    }
    
    public void setSavingsRate(double savingsRate) { 
        this.savingsRate = savingsRate; 
    }
    
    public double getExpenseToIncomeRatio() { 
        return expenseToIncomeRatio; 
    }
    
    public void setExpenseToIncomeRatio(double expenseToIncomeRatio) { 
        this.expenseToIncomeRatio = expenseToIncomeRatio; 
    }
    
    public int getOverdraftCount() { 
        return overdraftCount; 
    }
    
    public void setOverdraftCount(int overdraftCount) { 
        this.overdraftCount = overdraftCount; 
    }
    
    public int getGamblingTransactions() { 
        return gamblingTransactions; 
    }
    
    public void setGamblingTransactions(int gamblingTransactions) { 
        this.gamblingTransactions = gamblingTransactions; 
    }
    
    public int getInvestmentTransactions() { 
        return investmentTransactions; 
    }
    
    public void setInvestmentTransactions(int investmentTransactions) { 
        this.investmentTransactions = investmentTransactions; 
    }
    
    public double getIncomeGrowthTrend() { 
        return incomeGrowthTrend; 
    }
    
    public void setIncomeGrowthTrend(double incomeGrowthTrend) { 
        this.incomeGrowthTrend = incomeGrowthTrend; 
    }
    
    public int getTotalTransactionsAnalyzed() { 
        return totalTransactionsAnalyzed; 
    }
    
    public void setTotalTransactionsAnalyzed(int totalTransactionsAnalyzed) { 
        this.totalTransactionsAnalyzed = totalTransactionsAnalyzed; 
    }
    
    public int getMonthsAnalyzed() { 
        return monthsAnalyzed; 
    }
    
    public void setMonthsAnalyzed(int monthsAnalyzed) { 
        this.monthsAnalyzed = monthsAnalyzed; 
    }
}