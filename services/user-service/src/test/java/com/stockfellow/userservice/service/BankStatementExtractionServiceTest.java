package com.stockfellow.userservice.service;

import com.stockfellow.userservice.model.BankTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BankStatementExtractionServiceTest {

    // @InjectMocks
    // private BankStatementExtractionService bankStatementExtractionService;

    // @BeforeEach
    // void setUp() {
    //     // Test setup
    // }

    // @Test
    // void extractTransactionsFromText_FNBFormat_ShouldExtractCorrectly() {
    //     String fnbStatement = """
    //         01/10/2024    SALARY PAYMENT                 +15000.00    15000.00
    //         02/10/2024    GROCERIES PURCHASE             -500.00      14500.00
    //         03/10/2024    TRANSFER TO SAVINGS            -2000.00     12500.00
    //         04/10/2024    ELECTRICITY PAYMENT            -350.00      12150.00
    //         05/10/2024    FREELANCE INCOME               +1200.00     13350.00
    //         """;

    //     List<BankTransaction> transactions = bankStatementExtractionService.extractTransactionsFromText(fnbStatement);

    //     assertFalse(transactions.isEmpty());
    //     assertEquals(5, transactions.size());
        
    //     // Check first transaction
    //     BankTransaction firstTransaction = transactions.get(0);
    //     assertEquals(LocalDate.of(2024, 10, 1), firstTransaction.getDate());
    //     assertEquals("SALARY PAYMENT", firstTransaction.getDescription());
    //     assertEquals(15000.00, firstTransaction.getAmount(), 0.01);
    //     assertEquals(15000.00, firstTransaction.getBalance(), 0.01);
    //     assertEquals("CREDIT", firstTransaction.getType());
    // }

    // @Test
    // void extractTransactionsFromText_StandardBankFormat_ShouldExtractCorrectly() {
    //     String standardBankStatement = """
    //         01-10-2024    Monthly Salary Transfer         R15000.00    R15000.00
    //         02-10-2024    POS Purchase - Woolworths       -R500.00     R14500.00
    //         03-10-2024    Transfer to Investment Account  -R2000.00    R12500.00
    //         04-10-2024    Municipal Services Payment      -R350.00     R12150.00
    //         """;

    //     List<BankTransaction> transactions = bankStatementExtractionService.extractTransactionsFromText(standardBankStatement);

    //     assertFalse(transactions.isEmpty());
    //     assertEquals(4, transactions.size());
        
    //     // Verify amounts are parsed correctly
    //     assertEquals(15000.00, transactions.get(0).getAmount(), 0.01);
    //     assertEquals(-500.00, transactions.get(1).getAmount(), 0.01);
    // }

    // @Test
    // void extractTransactionsFromText_CapitecFormat_ShouldExtractCorrectly() {
    //     String capitecStatement = """
    //         2024/10/01    SALARY DEPOSIT                  R 15,000.00  R 15,000.00
    //         2024/10/02    GROCERY STORE                   -R 500.00    R 14,500.00
    //         2024/10/03    INVESTMENT CONTRIBUTION         -R 2,000.00  R 12,500.00
    //         """;

    //     List<BankTransaction> transactions = bankStatementExtractionService.extractTransactionsFromText(capitecStatement);

    //     assertFalse(transactions.isEmpty());
    //     assertEquals(3, transactions.size());
        
    //     // Check amount parsing with spaces and commas
    //     assertEquals(15000.00, transactions.get(0).getAmount(), 0.01);
    //     assertEquals(-500.00, transactions.get(1).getAmount(), 0.01);
    //     assertEquals(-2000.00, transactions.get(2).getAmount(), 0.01);
    // }

    // @Test
    // void extractTransactionsFromText_EmptyText_ShouldThrowException() {
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         bankStatementExtractionService.extractTransactionsFromText("");
    //     });
    // }

    // @Test
    // void extractTransactionsFromText_NoValidTransactions_ShouldReturnEmptyList() {
    //     String invalidText = "This is just some random text with no transaction data";
        
    //     List<BankTransaction> transactions = bankStatementExtractionService.extractTransactionsFromText(invalidText);
        
    //     assertTrue(transactions.isEmpty());
    // }

    // @Test
    // void extractTransactionsFromPdf_NullFile_ShouldThrowException() {
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         bankStatementExtractionService.extractTransactionsFromPdf(null);
    //     });
    // }

    // @Test
    // void extractTransactionsFromPdf_EmptyFile_ShouldThrowException() {
    //     MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         bankStatementExtractionService.extractTransactionsFromPdf(emptyFile);
    //     });
    // }

    // @Test
    // void analyzeExtractionQuality_SufficientTransactions_ShouldReturnHighScore() {
    //     // Create test transactions using ArrayList for mutability
    //     List<BankTransaction> transactions = new ArrayList<>();
    //     transactions.add(createTestTransaction(LocalDate.of(2024, 8, 1), "SALARY", 15000.00));
    //     transactions.add(createTestTransaction(LocalDate.of(2024, 8, 15), "GROCERIES", -500.00));
    //     transactions.add(createTestTransaction(LocalDate.of(2024, 9, 1), "SALARY", 15000.00));
    //     transactions.add(createTestTransaction(LocalDate.of(2024, 9, 15), "GROCERIES", -500.00));
    //     transactions.add(createTestTransaction(LocalDate.of(2024, 10, 1), "SALARY", 15000.00));

    //     // Add more transactions to reach 60
    //     for (int i = 0; i < 55; i++) {
    //         transactions.add(createTestTransaction(
    //             LocalDate.of(2024, 8 + (i % 3), 1 + (i % 28)), 
    //             "Transaction " + i, 
    //             -100.0 - i
    //         ));
    //     }

    //     BankStatementExtractionService.BankStatementAnalysisResult result = 
    //         bankStatementExtractionService.analyzeExtractionQuality(transactions, "test");

    //     assertTrue(result.getQualityScore() > 70.0);
    //     assertEquals(60, result.getTotalTransactionsExtracted());
    // }

    // @Test
    // void analyzeExtractionQuality_InsufficientTransactions_ShouldReturnLowScore() {
    //     List<BankTransaction> transactions = List.of(
    //         createTestTransaction(LocalDate.of(2024, 10, 1), "SALARY", 15000.00),
    //         createTestTransaction(LocalDate.of(2024, 10, 2), "GROCERIES", -500.00)
    //     );

    //     BankStatementExtractionService.BankStatementAnalysisResult result = 
    //         bankStatementExtractionService.analyzeExtractionQuality(transactions, "test");

    //     assertTrue(result.getQualityScore() < 50.0);
    //     assertTrue(result.getWarnings().size() > 0);
    // }

    // private BankTransaction createTestTransaction(LocalDate date, String description, double amount) {
    //     BankTransaction transaction = new BankTransaction();
    //     transaction.setDate(date);
    //     transaction.setDescription(description);
    //     transaction.setAmount(amount);
    //     transaction.setBalance(10000.0 + amount);
    //     transaction.setType(amount >= 0 ? "CREDIT" : "DEBIT");
    //     return transaction;
    // }
}
