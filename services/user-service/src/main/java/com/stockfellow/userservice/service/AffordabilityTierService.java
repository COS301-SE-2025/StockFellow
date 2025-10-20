// AffordabilityTierService.java
package com.stockfellow.userservice.service;

import com.stockfellow.userservice.model.BankTransaction;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.dto.AffordabilityTierResult;
import com.stockfellow.userservice.dto.BankStatementAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AffordabilityTierService {
    
    private static final Logger logger = LoggerFactory.getLogger(AffordabilityTierService.class);
    
    @Autowired
    private UserService userService;
    
    // Tier income thresholds (in ZAR)
    private static final Map<Integer, double[]> TIER_INCOME_RANGES = Map.of(
        1, new double[]{2000, 8000},    // Essential Savers
        2, new double[]{8000, 15000},   // Steady Builders  
        3, new double[]{15000, 25000},  // Balanced Savers
        4, new double[]{25000, 50000},  // Growth Investors
        5, new double[]{50000, 100000}, // Premium Accumulators
        6, new double[]{100000, Double.MAX_VALUE} // Elite Circle
    );
    
    private static final Map<Integer, int[]> TIER_CONTRIBUTION_RANGES = Map.of(
        1, new int[]{50, 200},
        2, new int[]{200, 500},
        3, new int[]{500, 1000},
        4, new int[]{1000, 2500},
        5, new int[]{2500, 5000},
        6, new int[]{5000, 10000}
    );
    
    public AffordabilityTierResult analyzeBankStatements(String userId, List<BankTransaction> transactions) {
        try
        {
            logger.info("Starting affordability analysis for user: {}", userId);
            
            // Validate input
            if (transactions == null) {
                throw new IllegalArgumentException("Bank statements are required for analysis");
            }
            
            if (transactions.isEmpty()) {
                throw new IllegalArgumentException("Bank statements are required for analysis");
            }
            
            if (transactions.size() < 20) {
                throw new IllegalArgumentException("Insufficient transaction data. Minimum 50 transactions required for reliable analysis");
            }

            // Get user details
            User user = userService.getUserByUserId(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId); // Change to IllegalArgumentException
            }
                  // Analyze transactions
            BankStatementAnalysis analysis = performBankStatementAnalysis(transactions);
            
            // Validate analysis results
            validateAnalysisResults(analysis);
            
            // Log analysis results for debugging
            logger.info("Bank statement analysis results for user {}:", userId);
            logger.info("- Average monthly income: R{}", analysis.getAverageMonthlyIncome());
            logger.info("- Average monthly expenses: R{}", analysis.getAverageMonthlyExpenses());
            logger.info("- Average monthly savings: R{}", analysis.getAverageMonthlySavings());
            logger.info("- Expense to income ratio: {}", analysis.getExpenseToIncomeRatio());
            logger.info("- Savings rate: {}", analysis.getSavingsRate());
            logger.info("- Overdraft count: {}", analysis.getOverdraftCount());
            logger.info("- Income stability: {}", analysis.getIncomeStability());
            
            // Calculate financial scores
            int incomeStabilityScore = calculateIncomeStabilityScore(analysis);
            int expenseManagementScore = calculateExpenseManagementScore(analysis);
            int savingsBehaviorScore = calculateSavingsBehaviorScore(analysis);
            int financialStabilityScore = calculateFinancialStabilityScore(analysis);
            
            logger.info("Financial scores for user {}:", userId);
            logger.info("- Income stability score: {}", incomeStabilityScore);
            logger.info("- Expense management score: {}", expenseManagementScore);
            logger.info("- Savings behavior score: {}", savingsBehaviorScore);
            logger.info("- Financial stability score: {}", financialStabilityScore);
            
            // Determine tier using if-statements
            int tier = determineTierWithRules(analysis, incomeStabilityScore, expenseManagementScore, 
                                           savingsBehaviorScore, financialStabilityScore);
            
            // Calculate confidence
            double confidence = calculateConfidenceScore(analysis, tier);
            
            // Build result
            AffordabilityTierResult result = buildTierResult(userId, tier, confidence, analysis, 
                                                           incomeStabilityScore, expenseManagementScore,
                                                           savingsBehaviorScore, financialStabilityScore);
            
            logger.info("User {} assigned to Tier {} with confidence: {}%", 
                       userId, tier, Math.round(confidence * 100));
            
            return result;
            
        } catch (IllegalArgumentException e) {
        // Re-throw IllegalArgumentException directly
        throw e;
        } catch (Exception e) {
            logger.error("Error analyzing affordability for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to analyze affordability: " + e.getMessage(), e);
        }
    }
    
    public BankStatementAnalysis performBankStatementAnalysis(List<BankTransaction> transactions) {
        BankStatementAnalysis analysis = new BankStatementAnalysis();
        
        // Create a mutable list for sorting
        List<BankTransaction> sortedTransactions = new ArrayList<>(transactions);
        sortedTransactions.sort(Comparator.comparing(BankTransaction::getDate));
        
        // Group by month
        Map<String, List<BankTransaction>> monthlyTransactions = groupTransactionsByMonth(sortedTransactions);
        
        List<Double> monthlyIncomes = new ArrayList<>();
        List<Double> monthlyExpenses = new ArrayList<>();
        List<Double> monthlySavings = new ArrayList<>();
        List<Double> monthlyBalances = new ArrayList<>();
        
        // Calculate monthly metrics
        for (Map.Entry<String, List<BankTransaction>> monthEntry : monthlyTransactions.entrySet()) {
            List<BankTransaction> monthTrans = monthEntry.getValue();
            
            double income = calculateMonthlyIncome(monthTrans);
            double expenses = calculateMonthlyExpenses(monthTrans);
            double savings = calculateMonthlySavings(monthTrans);
            double avgBalance = calculateAverageMonthlyBalance(monthTrans);
            
            if (income > 1000 && monthTrans.size() >= 15) { // Only count valid months
                monthlyIncomes.add(income);
                monthlyExpenses.add(expenses);
                monthlySavings.add(savings);
                monthlyBalances.add(avgBalance);
            }
        }
          // Set analysis metrics
        analysis.setAverageMonthlyIncome(calculateAverage(monthlyIncomes));
        analysis.setAverageMonthlyExpenses(calculateAverage(monthlyExpenses));
        analysis.setAverageMonthlySavings(calculateAverage(monthlySavings));
        analysis.setAverageBalance(calculateAverage(monthlyBalances));
        
        // Set additional metadata
        analysis.setTotalTransactionsAnalyzed(sortedTransactions.size());
        analysis.setMonthsAnalyzed(monthlyIncomes.size());
        
        // Calculate ratios and stability
        analysis.setIncomeStability(calculateStabilityScore(monthlyIncomes));
        if (analysis.getAverageMonthlyIncome() > 0) {
            analysis.setExpenseToIncomeRatio(analysis.getAverageMonthlyExpenses() / analysis.getAverageMonthlyIncome());
            analysis.setSavingsRate(analysis.getAverageMonthlySavings() / analysis.getAverageMonthlyIncome());
        } else {
            analysis.setExpenseToIncomeRatio(1.0); // Assume spending everything if no income detected
            analysis.setSavingsRate(0.0);
        }
        
        // Count special transactions
        analysis.setOverdraftCount(countOverdrafts(sortedTransactions));
        analysis.setGamblingTransactions(countGamblingTransactions(sortedTransactions));
        analysis.setInvestmentTransactions(countInvestmentTransactions(sortedTransactions));
        
        // Calculate trends
        analysis.setIncomeGrowthTrend(calculateIncomeGrowthTrend(monthlyIncomes));
        
        return analysis;
    }
    
    /**
     * Enhanced monthly transaction grouping with better date handling
     */
    private Map<String, List<BankTransaction>> groupTransactionsByMonth(List<BankTransaction> transactions) {
        Map<String, List<BankTransaction>> monthlyGroups = new LinkedHashMap<>();
        
        for (BankTransaction transaction : transactions) {
            if (transaction.getDate() != null) {
                String monthKey = transaction.getDate().getYear() + "-" + 
                                String.format("%02d", transaction.getDate().getMonthValue());
                
                monthlyGroups.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(transaction);
            }
        }
        
        // Remove months with insufficient data (less than 10 transactions)
        monthlyGroups.entrySet().removeIf(entry -> entry.getValue().size() < 10);
        
        logger.info("Grouped transactions into {} valid months", monthlyGroups.size());
        return monthlyGroups;
    }

    public int determineTierWithRules(BankStatementAnalysis analysis, int incomeScore, 
                                     int expenseScore, int savingsScore, int stabilityScore) {
        
        double monthlyIncome = analysis.getAverageMonthlyIncome();
        
        // Step 1: Determine base tier from income
        int baseTier = getBaseTierFromIncome(monthlyIncome);
        
        // Step 2: Apply financial behavior adjustments using if-statements
        int adjustedTier = baseTier;
        
        // Major red flags - immediate downgrades
        if (hasSeriosFinancialIssues(analysis)) {
            adjustedTier = Math.max(1, adjustedTier - 2);
            logger.info("Tier downgraded to {} due to serious financial issues", adjustedTier);
        } else if (hasModerateFinancialIssues(analysis)) {
            adjustedTier = Math.max(1, adjustedTier - 1);
            logger.info("Tier downgraded to {} due to moderate financial issues", adjustedTier);
        }
        
        // Excellent financial behavior - potential upgrades
        if (hasExcellentFinancialBehavior(incomeScore, expenseScore, savingsScore, stabilityScore)) {
            if (canUpgradeTier(baseTier, monthlyIncome)) {
                adjustedTier = Math.min(6, baseTier + 1);
                logger.info("Tier upgraded to {} due to excellent financial behavior", adjustedTier);
            }
        }
        
        // Good financial behavior with income support
        if (hasGoodFinancialBehavior(incomeScore, expenseScore, savingsScore, stabilityScore)) {
            if (canUpgradeTier(baseTier, monthlyIncome) && baseTier <= 2) {
                adjustedTier = Math.min(6, baseTier + 1);
                logger.info("Tier upgraded to {} due to good financial behavior", adjustedTier);
            }
        }
        
        // High earners with poor habits
        if (isHighEarnerWithPoorHabits(baseTier, expenseScore, stabilityScore)) {
            adjustedTier = Math.max(2, adjustedTier - 1);
            logger.info("High earner tier downgraded to {} due to poor financial habits", adjustedTier);
        }
        
        return adjustedTier;
    }
    
    private boolean hasSeriosFinancialIssues(BankStatementAnalysis analysis) {
        return analysis.getOverdraftCount() > 10 ||
               analysis.getGamblingTransactions() > 5 ||
               analysis.getExpenseToIncomeRatio() > 1.1 ||
               (analysis.getExpenseToIncomeRatio() > 0.95 && analysis.getSavingsRate() < 0.01);
    }
    
    private boolean hasModerateFinancialIssues(BankStatementAnalysis analysis) {
        return analysis.getOverdraftCount() > 5 ||
               analysis.getGamblingTransactions() > 2 ||
               analysis.getExpenseToIncomeRatio() > 0.9 ||
               (analysis.getSavingsRate() < 0.05 && analysis.getAverageBalance() < analysis.getAverageMonthlyExpenses());
    }
    
    private boolean hasExcellentFinancialBehavior(int incomeScore, int expenseScore, 
                                                int savingsScore, int stabilityScore) {
        int averageScore = (incomeScore + expenseScore + savingsScore + stabilityScore) / 4;
        return averageScore >= 80 && 
               incomeScore >= 70 && 
               expenseScore >= 70 && 
               savingsScore >= 60;
    }
    
    private boolean hasGoodFinancialBehavior(int incomeScore, int expenseScore, 
                                           int savingsScore, int stabilityScore) {
        int averageScore = (incomeScore + expenseScore + savingsScore + stabilityScore) / 4;
        return averageScore >= 65 && 
               incomeScore >= 50 && 
               expenseScore >= 60;
    }
    
    private boolean isHighEarnerWithPoorHabits(int baseTier, int expenseScore, int stabilityScore) {
        return baseTier >= 4 && (expenseScore < 40 || stabilityScore < 30);
    }
    
    private boolean canUpgradeTier(int currentTier, double income) {
        if (currentTier >= 6) return false;
        
        double[] nextTierRange = TIER_INCOME_RANGES.get(currentTier + 1);
        return income >= nextTierRange[0] * 0.8; // 80% of next tier minimum
    }
      private int getBaseTierFromIncome(double monthlyIncome) {
        logger.debug("Determining base tier for income: R{}", monthlyIncome);
        
        for (int tier = 6; tier >= 1; tier--) {
            double[] range = TIER_INCOME_RANGES.get(tier);
            logger.debug("Checking tier {}: range R{} - R{}", tier, range[0], 
                       range[1] == Double.MAX_VALUE ? "unlimited" : range[1]);
            
            if (monthlyIncome >= range[0]) {
                // Check if income falls within this tier's upper bound
                if (tier < 6) { // Not the highest tier
                    double[] nextTierRange = TIER_INCOME_RANGES.get(tier + 1);
                    if (monthlyIncome >= nextTierRange[0]) {
                        // Income qualifies for higher tier, continue to check higher tiers
                        continue;
                    }
                }
                
                logger.info("Base tier determined: {} for income R{}", tier, monthlyIncome);
                return tier;
            }
        }
        
        logger.info("Income R{} below minimum threshold, assigning tier 1", monthlyIncome);
        return 1;
    }
    
    public int calculateIncomeStabilityScore(BankStatementAnalysis analysis) {
        int score = 0;
        
        // Income level scoring
        if (analysis.getAverageMonthlyIncome() >= 50000) {
            score += 25;
        } else if (analysis.getAverageMonthlyIncome() >= 25000) {
            score += 20;
        } else if (analysis.getAverageMonthlyIncome() >= 15000) {
            score += 15;
        } else if (analysis.getAverageMonthlyIncome() >= 8000) {
            score += 10;
        } else if (analysis.getAverageMonthlyIncome() >= 2000) {
            score += 5;
        }
        
        // Stability scoring (lower coefficient of variation is better)
        if (analysis.getIncomeStability() <= 0.1) {
            score += 30; // Very stable
        } else if (analysis.getIncomeStability() <= 0.2) {
            score += 25; // Stable
        } else if (analysis.getIncomeStability() <= 0.3) {
            score += 15; // Moderately stable
        } else if (analysis.getIncomeStability() <= 0.5) {
            score += 10; // Unstable
        }
        
        // Growth trend
        if (analysis.getIncomeGrowthTrend() > 0.1) {
            score += 20; // Strong growth
        } else if (analysis.getIncomeGrowthTrend() > 0.05) {
            score += 15; // Moderate growth
        } else if (analysis.getIncomeGrowthTrend() > 0) {
            score += 10; // Slight growth
        } else if (analysis.getIncomeGrowthTrend() >= -0.05) {
            score += 5; // Stable
        }
        
        // Investment income bonus
        if (analysis.getInvestmentTransactions() > 0) {
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    public int calculateExpenseManagementScore(BankStatementAnalysis analysis) {
        int score = 0;
        
        // Expense ratio scoring
        double expenseRatio = analysis.getExpenseToIncomeRatio();
        if (expenseRatio <= 0.4) {
            score += 40; // Excellent control
        } else if (expenseRatio <= 0.6) {
            score += 30; // Good control
        } else if (expenseRatio <= 0.8) {
            score += 20; // Fair control
        } else if (expenseRatio <= 0.9) {
            score += 10; // Poor control
        } else if (expenseRatio <= 1.0) {
            score += 5; // Very poor control
        }
        
        // Overdraft penalty
        if (analysis.getOverdraftCount() == 0) {
            score += 25; // No overdrafts
        } else if (analysis.getOverdraftCount() <= 2) {
            score += 15; // Rare overdrafts
        } else if (analysis.getOverdraftCount() <= 5) {
            score += 5; // Occasional overdrafts
        }
        
        // Gambling penalty
        if (analysis.getGamblingTransactions() == 0) {
            score += 20; // No gambling
        } else if (analysis.getGamblingTransactions() <= 2) {
            score += 10; // Rare gambling
        }
        
        // Expense stability bonus
        if (analysis.getIncomeStability() <= 0.3) { // Using income stability as proxy for expense predictability
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    private int calculateSavingsBehaviorScore(BankStatementAnalysis analysis) {
        int score = 0;
        
        // Savings rate scoring
        double savingsRate = analysis.getSavingsRate();
        if (savingsRate >= 0.25) {
            score += 35; // Excellent savings rate
        } else if (savingsRate >= 0.2) {
            score += 30; // Very good savings rate
        } else if (savingsRate >= 0.15) {
            score += 25; // Good savings rate
        } else if (savingsRate >= 0.1) {
            score += 20; // Fair savings rate
        } else if (savingsRate >= 0.05) {
            score += 15; // Poor savings rate
        } else if (savingsRate > 0) {
            score += 10; // Very poor savings rate
        }
        
        // Emergency fund scoring
        double emergencyMonths = analysis.getAverageBalance() / analysis.getAverageMonthlyExpenses();
        if (emergencyMonths >= 6) {
            score += 25; // Excellent emergency fund
        } else if (emergencyMonths >= 3) {
            score += 20; // Good emergency fund
        } else if (emergencyMonths >= 1) {
            score += 15; // Basic emergency fund
        } else if (emergencyMonths >= 0.5) {
            score += 10; // Minimal emergency fund
        }
        
        // Investment activity bonus
        if (analysis.getInvestmentTransactions() >= 10) {
            score += 20; // Very active investor
        } else if (analysis.getInvestmentTransactions() >= 5) {
            score += 15; // Active investor
        } else if (analysis.getInvestmentTransactions() > 0) {
            score += 10; // Some investment activity
        }
        
        // Financial cushion bonus
        if (analysis.getAverageBalance() > analysis.getAverageMonthlyIncome() * 2) {
            score += 20; // Very strong cushion
        } else if (analysis.getAverageBalance() > analysis.getAverageMonthlyIncome()) {
            score += 15; // Strong cushion
        } else if (analysis.getAverageBalance() > 0) {
            score += 10; // Some cushion
        }
        
        return Math.min(100, score);
    }
    
    private int calculateFinancialStabilityScore(BankStatementAnalysis analysis) {
        int score = 0;
        
        // Balance scoring
        if (analysis.getAverageBalance() > analysis.getAverageMonthlyExpenses() * 3) {
            score += 30; // Very strong balance
        } else if (analysis.getAverageBalance() > analysis.getAverageMonthlyExpenses() * 2) {
            score += 25; // Strong balance
        } else if (analysis.getAverageBalance() > analysis.getAverageMonthlyExpenses()) {
            score += 20; // Good balance
        } else if (analysis.getAverageBalance() > 0) {
            score += 15; // Positive balance
        }
        
        // Overdraft stability
        if (analysis.getOverdraftCount() == 0) {
            score += 25; // No overdrafts
        } else if (analysis.getOverdraftCount() <= 2) {
            score += 15; // Rare overdrafts
        } else if (analysis.getOverdraftCount() <= 5) {
            score += 5; // Occasional overdrafts
        }
        
        // Income surplus
        double surplus = analysis.getAverageMonthlyIncome() - analysis.getAverageMonthlyExpenses();
        double surplusRatio = surplus / analysis.getAverageMonthlyIncome();
        if (surplusRatio >= 0.3) {
            score += 20; // Large surplus
        } else if (surplusRatio >= 0.2) {
            score += 15; // Good surplus
        } else if (surplusRatio >= 0.1) {
            score += 10; // Basic surplus
        } else if (surplusRatio > 0) {
            score += 5; // Small surplus
        }
        
        // Investment sophistication
        if (analysis.getInvestmentTransactions() > 0) {
            score += 15; // Shows financial planning
        }
        
        // Data quality bonus (sufficient transaction history)
        score += 10; // Base score for having adequate data
        
        return Math.min(100, score);
    }
    
    // Helper methods for transaction analysis
    
    private double calculateMonthlyIncome(List<BankTransaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getAmount() > 0)
            .filter(this::isIncomeTransaction)
            .mapToDouble(BankTransaction::getAmount)
            .sum();
    }
    
    private double calculateMonthlyExpenses(List<BankTransaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getAmount() < 0)
            .filter(t -> !isSavingsTransaction(t))
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
    }
    
    private double calculateMonthlySavings(List<BankTransaction> transactions) {
        return transactions.stream()
            .filter(this::isSavingsTransaction)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
    }
    
    private double calculateAverageMonthlyBalance(List<BankTransaction> transactions) {
        return transactions.stream()
            .mapToDouble(BankTransaction::getBalance)
            .average()
            .orElse(0.0);
    }
      private boolean isIncomeTransaction(BankTransaction transaction) {
        String desc = transaction.getDescription().toLowerCase();
        double amount = transaction.getAmount();
        
        // Income must be positive
        if (amount <= 0) {
            return false;
        }
          // Check for explicit income keywords
        String[] incomeKeywords = {
            "salary", "wage", "wages", "income", "bonus", "commission",
            "dividend", "interest", "pension", "grant", "allowance",
            "stipend", "payroll", "payment from", "transfer from",
            "deposit", "credit", "refund", "rebate", "cashback",
            "freelance", "consulting", "contractor", "salary payment",
            "employer", "pty ltd", "company"
        };
        
        for (String keyword : incomeKeywords) {
            if (desc.contains(keyword)) {
                logger.debug("Income transaction identified by keyword '{}': {} - R{}", 
                           keyword, desc, amount);
                return true;
            }
        }
        
        // Check for patterns that indicate income
        // Large round amounts (likely salary)
        if (amount >= 5000 && (amount % 100 == 0 || amount % 50 == 0)) {
            logger.debug("Income transaction identified by amount pattern: {} - R{}", desc, amount);
            return true;
        }
        
        // Regular monthly amounts (potential salary)
        if (amount >= 3000 && amount <= 150000) {
            // Check if it looks like a regular payment
            if (desc.length() < 50 && !desc.contains("payment") && !desc.contains("transfer to")) {
                logger.debug("Income transaction identified as potential salary: {} - R{}", desc, amount);
                return true;
            }
        }
          // Exclude obvious non-income transactions (but be careful with salary payments)
        String[] excludeKeywords = {
            "withdrawal", "purchase", "debit", "fee", "charge",
            "transfer to", "sent to", "atm", "pos", "eft", "stop order",
            "debit order", "insurance", "medical aid", "loan", "credit card"
        };
        
        for (String keyword : excludeKeywords) {
            if (desc.contains(keyword)) {
                // Don't exclude if it's clearly a salary payment
                if (keyword.equals("payment") && (desc.contains("salary") || desc.contains("wage") || desc.contains("employer"))) {
                    continue; // Allow salary payments through
                }
                return false; // Definitely not income
            }
        }
        
        // For amounts over R1000, be more inclusive
        if (amount >= 1000) {
            logger.debug("Potential income transaction (large amount): {} - R{}", desc, amount);
            return true;
        }
        
        return false;
    }
      private boolean isSavingsTransaction(BankTransaction transaction) {
        String desc = transaction.getDescription().toLowerCase();
        double amount = Math.abs(transaction.getAmount()); // Consider both debit and credit
        
        // Direct savings keywords
        String[] savingsKeywords = {
            "savings", "save", "investment", "invest", "unit trust",
            "fixed deposit", "fd", "tfsa", "tax free", "retirement",
            "pension", "provident", "money market", "call account",
            "notice account", "endowment", "policy", "fund",
            "portfolio", "shares", "equity", "bond", "etf"
        };
        
        for (String keyword : savingsKeywords) {
            if (desc.contains(keyword)) {
                logger.debug("Savings transaction identified by keyword '{}': {} - R{}", 
                           keyword, desc, amount);
                return true;
            }
        }
        
        // Transfer patterns that might indicate savings
        if (desc.contains("transfer to") || desc.contains("transfer from")) {
            // Exclude obvious non-savings transfers
            if (!desc.contains("credit card") && !desc.contains("loan") && 
                !desc.contains("current") && !desc.contains("cheque")) {
                // Large transfers might be savings
                if (amount >= 500) {
                    logger.debug("Savings transaction identified as transfer: {} - R{}", desc, amount);
                    return true;
                }
            }
        }
        
        // Regular debit orders that might be investments
        if (desc.contains("debit order") || desc.contains("stop order")) {
            // Look for investment-related companies
            if (desc.contains("allan gray") || desc.contains("coronation") ||
                desc.contains("investec") || desc.contains("momentum") ||
                desc.contains("old mutual") || desc.contains("sanlam") ||
                desc.contains("liberty") || desc.contains("discovery") ||
                desc.contains("standard bank") || desc.contains("fnb") ||
                desc.contains("absa") || desc.contains("nedbank")) {
                logger.debug("Savings transaction identified as investment debit order: {} - R{}", desc, amount);
                return true;
            }
        }
        
        return false;
    }
    
    private int countOverdrafts(List<BankTransaction> transactions) {
        return (int) transactions.stream()
            .filter(t -> t.getBalance() < 0)
            .count();
    }
    
    private int countGamblingTransactions(List<BankTransaction> transactions) {
        return (int) transactions.stream()
            .filter(t -> {
                String desc = t.getDescription().toLowerCase();
                return desc.contains("bet") || desc.contains("casino") || 
                       desc.contains("lottery") || desc.contains("gambling");
            })
            .count();
    }
    
    private int countInvestmentTransactions(List<BankTransaction> transactions) {
        return (int) transactions.stream()
            .filter(t -> {
                String desc = t.getDescription().toLowerCase();
                return desc.contains("investment") || desc.contains("shares") ||
                       desc.contains("etf") || desc.contains("unit trust");
            })
            .count();
    }
    
    private double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private double calculateStabilityScore(List<Double> values) {
        if (values.size() < 2) return 1.0; // Perfect stability if only one value
        
        double mean = calculateAverage(values);
        if (mean == 0) return 1.0;
        
        double variance = values.stream()
            .mapToDouble(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
            
        double stdDev = Math.sqrt(variance);
        return stdDev / mean; // Coefficient of variation
    }
    
    private double calculateIncomeGrowthTrend(List<Double> monthlyIncomes) {
        if (monthlyIncomes.size() < 6) return 0.0;
        
        int size = monthlyIncomes.size();
        double recentAvg = monthlyIncomes.subList(size - 3, size).stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double previousAvg = monthlyIncomes.subList(size - 6, size - 3).stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);
            
        if (previousAvg == 0) return 0.0;
        return (recentAvg - previousAvg) / previousAvg;
    }
    
    private double calculateConfidenceScore(BankStatementAnalysis analysis, int tier) {
        double confidence = 0.75; // Base confidence
        
        // Increase confidence for stable patterns
        if (analysis.getIncomeStability() < 0.2) confidence += 0.1;
        if (analysis.getOverdraftCount() == 0) confidence += 0.05;
        if (analysis.getGamblingTransactions() == 0) confidence += 0.05;
        
        // Decrease confidence for edge cases
        double income = analysis.getAverageMonthlyIncome();
        double[] tierRange = TIER_INCOME_RANGES.get(tier);
        
        if (income < tierRange[0] * 1.2 || (tierRange[1] != Double.MAX_VALUE && income > tierRange[1] * 0.8)) {
            confidence -= 0.15; // Near tier boundaries
        }
        
        return Math.max(0.4, Math.min(0.95, confidence));
    }
    
    private AffordabilityTierResult buildTierResult(String userId, int tier, double confidence,
                                                  BankStatementAnalysis analysis, int incomeScore,
                                                  int expenseScore, int savingsScore, int stabilityScore) {
        
        AffordabilityTierResult result = new AffordabilityTierResult();
        result.setUserId(userId);
        result.setTier(tier);
        result.setTierName(getTierName(tier));
        result.setConfidence(confidence);
        
        int[] contributionRange = TIER_CONTRIBUTION_RANGES.get(tier);
        result.setRecommendedContributionMin(contributionRange[0]);
        result.setRecommendedContributionMax(contributionRange[1]);
        
        result.setAnalysis(analysis);
        result.setScores(Map.of(
            "incomeStability", incomeScore,
            "expenseManagement", expenseScore,
            "savingsBehavior", savingsScore,
            "financialStability", stabilityScore,
            "compositeScore", (incomeScore + expenseScore + savingsScore + stabilityScore) / 4
        ));
        
        result.setRiskFactors(identifyRiskFactors(analysis));
        result.setRecommendations(generateRecommendations(tier, analysis));
        result.setAnalyzedAt(new Date());
        
        return result;
    }
    
    private String getTierName(int tier) {
        switch (tier) {
            case 1: return "Essential Savers";
            case 2: return "Steady Builders";
            case 3: return "Balanced Savers";
            case 4: return "Growth Investors";
            case 5: return "Premium Accumulators";
            case 6: return "Elite Circle";
            default: return "Unknown Tier";
        }
    }
    
    private List<String> identifyRiskFactors(BankStatementAnalysis analysis) {
        List<String> risks = new ArrayList<>();
        
        if (analysis.getOverdraftCount() > 5) {
            risks.add("Frequent overdrafts detected (" + analysis.getOverdraftCount() + " instances)");
        }
        
        if (analysis.getGamblingTransactions() > 0) {
            risks.add("Gambling activity detected (" + analysis.getGamblingTransactions() + " transactions)");
        }
        
        if (analysis.getExpenseToIncomeRatio() > 0.9) {
            risks.add("High expense-to-income ratio (" + Math.round(analysis.getExpenseToIncomeRatio() * 100) + "%)");
        }
        
        if (analysis.getSavingsRate() < 0.05) {
            risks.add("Low savings rate (" + Math.round(analysis.getSavingsRate() * 100) + "%)");
        }
        
        if (analysis.getIncomeStability() > 0.4) {
            risks.add("Unstable income pattern");
        }
        
        return risks;
    }
    
    private List<String> generateRecommendations(int tier, BankStatementAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        
        int[] range = TIER_CONTRIBUTION_RANGES.get(tier);
        recommendations.add("Recommended monthly contribution: R" + range[0] + " - R" + range[1]);
        
        if (analysis.getSavingsRate() < 0.1) {
            recommendations.add("Increase savings rate to at least 10% of income");
        }
        
        if (analysis.getOverdraftCount() > 0) {
            recommendations.add("Build emergency fund to avoid overdrafts");
        }
        
        if (analysis.getInvestmentTransactions() == 0 && tier >= 3) {
            recommendations.add("Consider starting investment portfolio for long-term growth");
        }
        
        if (analysis.getExpenseToIncomeRatio() > 0.8) {
            recommendations.add("Review and optimize monthly expenses");
        }
        
        recommendations.add("Join groups with " + getTierGroupSize(tier) + " members for optimal dynamics");
        
        return recommendations;
    }
    
    private String getTierGroupSize(int tier) {
        switch (tier) {
            case 1: return "8-12";
            case 2: return "10-15";
            case 3: return "12-18";
            case 4: return "15-20";
            case 5: return "8-12";
            case 6: return "6-10";
            default: return "10-15";
        }
    }

    /**
     * Validate the analysis results to ensure they make sense
     */
    private void validateAnalysisResults(BankStatementAnalysis analysis) {
        List<String> issues = new ArrayList<>();
        
        if (analysis.getAverageMonthlyIncome() <= 0) {
            issues.add("No income detected in bank statement");
        }
        
        if (analysis.getExpenseToIncomeRatio() > 2.0) {
            issues.add("Expenses exceed income by more than 100% - possible data extraction error");
        }
        
        if (analysis.getSavingsRate() > 0.8) {
            issues.add("Savings rate above 80% - possible misclassification of transactions");
        }
        
        if (analysis.getMonthsAnalyzed() < 2) {
            issues.add("Insufficient months of data for reliable analysis");
        }
        
        if (!issues.isEmpty()) {
            logger.warn("Analysis validation issues found: {}", String.join(", ", issues));
        } else {
            logger.info("Analysis validation passed successfully");
        }
    }
}