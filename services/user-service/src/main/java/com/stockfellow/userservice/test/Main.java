package com.stockfellow.userservice.test;

import com.stockfellow.userservice.model.BankTransaction;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.BankStatementExtractionService;
import com.stockfellow.userservice.service.AffordabilityTierService;
import com.stockfellow.userservice.service.UserService;
import com.stockfellow.userservice.dto.AffordabilityTierResult;
import com.stockfellow.userservice.dto.BankStatementAnalysis;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Standalone test class to test bank statement extraction and tier calculation
 * without compiling the entire Spring Boot application.
 * 
 * Usage: java Main <path-to-pdf-file>
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <path-to-pdf-file>");
            System.out.println("Example: java Main /path/to/bank-statement.pdf");
            System.exit(1);
        }
        
        String pdfPath = args[0];
        File pdfFile = new File(pdfPath);
        
        if (!pdfFile.exists()) {
            System.err.println("Error: PDF file not found: " + pdfPath);
            System.exit(1);
        }
        
        if (!pdfFile.isFile()) {
            System.err.println("Error: Path is not a file: " + pdfPath);
            System.exit(1);
        }
        
        System.out.println("========================================");
        System.out.println("Bank Statement Affordability Analysis");
        System.out.println("========================================");
        System.out.println("PDF File: " + pdfFile.getAbsolutePath());
        System.out.println();
        
        try {
            // Step 1: Extract transactions from PDF
            System.out.println("[1/3] Extracting transactions from PDF...");
            BankStatementExtractionService extractionService = new BankStatementExtractionService();
            MockMultipartFile multipartFile = new MockMultipartFile(pdfFile);
            List<BankTransaction> transactions = extractionService.extractTransactionsFromPdf(multipartFile);
            
            System.out.println("✓ Extracted " + transactions.size() + " transactions");
            System.out.println();
            
            // Show sample transactions
            if (!transactions.isEmpty()) {
                System.out.println("Sample transactions (first 5):");
                int count = Math.min(5, transactions.size());
                for (int i = 0; i < count; i++) {
                    BankTransaction t = transactions.get(i);
                    System.out.printf("  %s | %-40s | R%,.2f | Balance: R%,.2f%n",
                        t.getDate(), 
                        truncate(t.getDescription(), 40),
                        t.getAmount(),
                        t.getBalance()
                    );
                }
                System.out.println();
            }
            
            // Step 2: Analyze bank statement
            System.out.println("[2/3] Analyzing bank statement...");
            AffordabilityTierService tierService = createTierServiceWithMock();
            
            BankStatementAnalysis analysis = tierService.performBankStatementAnalysis(transactions);
            
            System.out.println("✓ Analysis complete");
            System.out.println();
            printAnalysis(analysis);
            
            // Step 3: Calculate affordability tier
            System.out.println("[3/3] Calculating affordability tier...");
            AffordabilityTierResult result = tierService.analyzeBankStatements("test-user-123", transactions);
            
            System.out.println("✓ Tier calculation complete");
            System.out.println();
            printTierResult(result);
            
        } catch (IllegalArgumentException e) {
            System.err.println();
            System.err.println("❌ Validation Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println();
            System.err.println("❌ I/O Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("Analysis completed successfully!");
        System.out.println("========================================");
    }
    
    private static void printAnalysis(BankStatementAnalysis analysis) {
        System.out.println("Financial Analysis Summary:");
        System.out.println("─────────────────────────────────────────");
        System.out.printf("  Average Monthly Income:    R%,12.2f%n", analysis.getAverageMonthlyIncome());
        System.out.printf("  Average Monthly Expenses:  R%,12.2f%n", analysis.getAverageMonthlyExpenses());
        System.out.printf("  Average Monthly Savings:   R%,12.2f%n", analysis.getAverageMonthlySavings());
        System.out.printf("  Average Balance:           R%,12.2f%n", analysis.getAverageBalance());
        System.out.println();
        System.out.printf("  Expense to Income Ratio:   %.1f%%%n", analysis.getExpenseToIncomeRatio() * 100);
        System.out.printf("  Savings Rate:              %.1f%%%n", analysis.getSavingsRate() * 100);
        System.out.printf("  Income Stability:          %.3f%n", analysis.getIncomeStability());
        System.out.printf("  Income Growth Trend:       %.1f%%%n", analysis.getIncomeGrowthTrend() * 100);
        System.out.println();
        System.out.printf("  Overdraft Count:           %d%n", analysis.getOverdraftCount());
        System.out.printf("  Gambling Transactions:     %d%n", analysis.getGamblingTransactions());
        System.out.printf("  Investment Transactions:   %d%n", analysis.getInvestmentTransactions());
        System.out.println();
        System.out.printf("  Total Transactions:        %d%n", analysis.getTotalTransactionsAnalyzed());
        System.out.printf("  Months Analyzed:           %d%n", analysis.getMonthsAnalyzed());
        System.out.println();
    }
    
    private static void printTierResult(AffordabilityTierResult result) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     AFFORDABILITY TIER ASSESSMENT      ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf("║  Tier: %d - %-30s ║%n", result.getTier(), result.getTierName());
        System.out.printf("║  Confidence: %.1f%%                       ║%n", result.getConfidence() * 100);
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf("║  Recommended Contribution:             ║%n");
        System.out.printf("║    R%,d - R%,d per month          ║%n", 
            result.getRecommendedContributionMin(),
            result.getRecommendedContributionMax());
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        
        // Print scores
        Map<String, Integer> scores = result.getScores();
        System.out.println("Financial Scores:");
        System.out.println("─────────────────────────────────────────");
        System.out.printf("  Income Stability:      %3d/100%n", scores.get("incomeStability"));
        System.out.printf("  Expense Management:    %3d/100%n", scores.get("expenseManagement"));
        System.out.printf("  Savings Behavior:      %3d/100%n", scores.get("savingsBehavior"));
        System.out.printf("  Financial Stability:   %3d/100%n", scores.get("financialStability"));
        System.out.printf("  Composite Score:       %3d/100%n", scores.get("compositeScore"));
        System.out.println();
        
        // Print risk factors
        if (result.getRiskFactors() != null && !result.getRiskFactors().isEmpty()) {
            System.out.println("⚠ Risk Factors:");
            System.out.println("─────────────────────────────────────────");
            for (String risk : result.getRiskFactors()) {
                System.out.println("  • " + risk);
            }
            System.out.println();
        }
        
        // Print recommendations
        if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
            System.out.println("💡 Recommendations:");
            System.out.println("─────────────────────────────────────────");
            for (String recommendation : result.getRecommendations()) {
                System.out.println("  • " + recommendation);
            }
            System.out.println();
        }
    }
    
    private static String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
    
    /**
     * Mock implementation of MultipartFile for testing
     */
    static class MockMultipartFile implements MultipartFile {
        private final File file;
        
        public MockMultipartFile(File file) {
            this.file = file;
        }
        
        @Override
        public String getName() {
            return file.getName();
        }
        
        @Override
        public String getOriginalFilename() {
            return file.getName();
        }
        
        @Override
        public String getContentType() {
            return "application/pdf";
        }
        
        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }
        
        @Override
        public long getSize() {
            return file.length();
        }
        
        @Override
        public byte[] getBytes() throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                return fis.readAllBytes();
            }
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }
        
        @Override
        public void transferTo(File dest) throws IOException {
            throw new UnsupportedOperationException("Not implemented for test");
        }
    }
    
    /**
     * Create AffordabilityTierService with mocked UserService using reflection
     */
    private static AffordabilityTierService createTierServiceWithMock() {
        AffordabilityTierService tierService = new AffordabilityTierService();
        
        try {
            // Use reflection to set the private userService field
            java.lang.reflect.Field userServiceField = AffordabilityTierService.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(tierService, new MockUserService());
        } catch (Exception e) {
            System.err.println("Warning: Could not inject mock UserService: " + e.getMessage());
            System.err.println("Analysis will proceed but may fail if UserService is called");
        }
        
        return tierService;
    }
    
    /**
     * Mock implementation of UserService for testing
     */
    static class MockUserService extends UserService {
        @Override
        public User getUserByUserId(String userId) {
            // Create a mock user for testing
            User user = new User();
            user.setUserId(userId);
            user.setUsername("test-user");
            user.setEmail("test@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            return user;
        }
    }
}
