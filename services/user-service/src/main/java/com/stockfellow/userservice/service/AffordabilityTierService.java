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
            
            if (transactions.size() < 50) {
                throw new IllegalArgumentException("Insufficient transaction data. Minimum 50 transactions required for reliable analysis");
            }

            // Get user details
            User user = userService.getUserByUserId(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId); // Change to IllegalArgumentException
            }
                
            // Analyze transactions
            BankStatementAnalysis analysis = performBankStatementAnalysis(transactions);
            
            // Calculate financial scores
            int incomeStabilityScore = calculateIncomeStabilityScore(analysis);
            int expenseManagementScore = calculateExpenseManagementScore(analysis);
            int savingsBehaviorScore = calculateSavingsBehaviorScore(analysis);
            int financialStabilityScore = calculateFinancialStabilityScore(analysis);
            
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
        Map<String, List<BankTransaction>> monthlyTransactions = sortedTransactions.stream()
            .collect(Collectors.groupingBy(t -> t.getDate().toString().substring(0, 7)));
        
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
        
        // Calculate ratios and stability
        analysis.setIncomeStability(calculateStabilityScore(monthlyIncomes));
        analysis.setExpenseToIncomeRatio(analysis.getAverageMonthlyExpenses() / analysis.getAverageMonthlyIncome());
        analysis.setSavingsRate(analysis.getAverageMonthlySavings() / analysis.getAverageMonthlyIncome());
        
        // Count special transactions
        analysis.setOverdraftCount(countOverdrafts(sortedTransactions));
        analysis.setGamblingTransactions(countGamblingTransactions(sortedTransactions));
        analysis.setInvestmentTransactions(countInvestmentTransactions(sortedTransactions));
        
        // Calculate trends
        analysis.setIncomeGrowthTrend(calculateIncomeGrowthTrend(monthlyIncomes));
        
        return analysis;
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
        for (int tier = 6; tier >= 1; tier--) {
            double[] range = TIER_INCOME_RANGES.get(tier);
            if (monthlyIncome >= range[0]) {
                // For tier boundaries, ensure we're not including values that are too close to the next tier
                if (tier < 6 && monthlyIncome >= TIER_INCOME_RANGES.get(tier + 1)[0] * 0.9) {
                    // If income is within 90% of next tier, consider it for the next tier
                    continue;
                }
                return tier;
            }
        }
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
        
        // Check for salary/wage keywords
        if (desc.contains("salary") || desc.contains("wage") || desc.contains("income") ||
            desc.contains("bonus") || desc.contains("dividend") || desc.contains("interest")) {
            return true;
        }
        
        // Large round amounts likely to be salary
        if (amount > 5000 && amount % 100 == 0) {
            return true;
        }
        
        return false;
    }
    
    private boolean isSavingsTransaction(BankTransaction transaction) {
        String desc = transaction.getDescription().toLowerCase();
        return desc.contains("savings") || desc.contains("investment") ||
               desc.contains("transfer to") || desc.contains("fixed deposit") ||
               desc.contains("unit trust") || desc.contains("tfsa");
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
}