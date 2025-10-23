package com.stockfellow.userservice.service;

import com.stockfellow.userservice.model.BankTransaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BankStatementExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankStatementExtractionService.class);
    
    // Enhanced patterns for different South African banks
    private static final Map<String, BankPatterns> BANK_PATTERNS = Map.of(
        "FNB", new BankPatterns(
            Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
            new String[]{"dd/MM/yyyy", "dd/MM/yy"},
            "FNB"
        ),
        "STANDARD_BANK", new BankPatterns(
            Pattern.compile("(\\d{2}-\\d{2}-\\d{4})\\s+(.{20,})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
            new String[]{"dd-MM-yyyy", "dd-MM-yy"},
            "Standard Bank"
        ),
        "ABSA", new BankPatterns(
            Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
            new String[]{"dd/MM/yyyy", "yyyy/MM/dd"},
            "ABSA"
        ),
        "NEDBANK", new BankPatterns(
            Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s+(.+?)\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
            new String[]{"yyyy-MM-dd", "dd-MM-yyyy"},
            "Nedbank"
        ),
        "CAPITEC", new BankPatterns(
            Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
            new String[]{"dd/MM/yyyy", "yyyy/MM/dd"},
            "Capitec"
        )
    );
    
    // Fallback patterns for generic bank statements
    private static final Pattern[] FALLBACK_PATTERNS = {
        // Single-line patterns
        Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\s+(.{10,})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
        Pattern.compile("(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})\\s+(.{10,})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE),
        Pattern.compile("(\\d{1,2}\\s+\\w{3}\\s+\\d{2,4})\\s+(.{10,})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s+([+-]?R?\\s*[\\d,]+\\.\\d{2})", Pattern.MULTILINE)
    };
    
    // Common date formats used by SA banks (4-digit year formats first for preference)
    private static final String[] COMMON_DATE_FORMATS = {
        "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "yyyy/MM/dd",
        "d/M/yyyy", "d-M-yyyy", "yyyy-M-d", "yyyy/M/d",
        "dd MMM yyyy", "d MMM yyyy", "MMM dd, yyyy",
        "dd/MM/yy", "dd-MM-yy", "d/M/yy", "d-M-yy",
        "yy/MM/dd", "yy-MM-dd", "yy/M/d", "yy-M-d",
        "dd MMM yy", "d MMM yy"
    };
    
    /**
     * Extract bank transactions from a PDF bank statement
     */
    public List<BankTransaction> extractTransactionsFromPdf(MultipartFile pdfFile) throws IOException {
        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        
        logger.info("Starting bank statement extraction from PDF: {}", pdfFile.getOriginalFilename());
        
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            // Try Tabula table extraction first (more robust for structured tables)
            List<BankTransaction> transactions = extractUsingTabula(document);
            
            if (!transactions.isEmpty()) {
                logger.info("Successfully extracted {} transactions using Tabula table extraction", transactions.size());
                return transactions;
            }
            
            // Fallback to text-based extraction
            logger.info("Tabula extraction yielded no results, falling back to text-based extraction");
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            return extractTransactionsFromText(text);
        }
    }
    
    /**
     * Extract transactions using Tabula library for robust table extraction
     */
    private List<BankTransaction> extractUsingTabula(PDDocument document) {
        List<BankTransaction> transactions = new ArrayList<>();
        
        try {
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm algorithm = new SpreadsheetExtractionAlgorithm();
            
            // Process each page
            for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
                Page page = extractor.extract(pageNum);
                List<Table> tables = algorithm.extract(page);
                
                logger.debug("Found {} tables on page {}", tables.size(), pageNum);
                
                for (Table table : tables) {
                    List<BankTransaction> pageTransactions = extractTransactionsFromTable(table);
                    transactions.addAll(pageTransactions);
                }
            }
            
            extractor.close();
            
        } catch (Exception e) {
            logger.warn("Tabula extraction failed: {}", e.getMessage());
        }
        
        return transactions;
    }
    
    /**
     * Extract transactions from a Tabula table
     */
    private List<BankTransaction> extractTransactionsFromTable(Table table) {
        List<BankTransaction> transactions = new ArrayList<>();
        
        List<List<RectangularTextContainer>> rows = table.getRows();
        
        if (rows.isEmpty()) {
            return transactions;
        }
        
        // Try to identify columns (Date, Description, Amount, Balance)
        int dateCol = -1, descCol = -1, amountCol = -1, balanceCol = -1;
        
        // Check first few rows for headers
        for (int i = 0; i < Math.min(3, rows.size()); i++) {
            List<RectangularTextContainer> row = rows.get(i);
            
            for (int j = 0; j < row.size(); j++) {
                String cellText = row.get(j).getText().toLowerCase().trim();
                
                if (cellText.contains("date") && dateCol == -1) {
                    dateCol = j;
                } else if (cellText.contains("description") && descCol == -1) {
                    descCol = j;
                } else if (cellText.contains("amount") && amountCol == -1) {
                    amountCol = j;
                } else if (cellText.contains("balance") && balanceCol == -1) {
                    balanceCol = j;
                }
            }
            
            // If we found headers, start data from next row
            if (dateCol != -1 && descCol != -1) {
                rows = rows.subList(i + 1, rows.size());
                break;
            }
        }
        
        // If we didn't find headers, try to auto-detect based on content
        if (dateCol == -1 || descCol == -1) {
            dateCol = 0;  // First column is usually date
            descCol = 1;  // Second column is usually description
            amountCol = rows.get(0).size() >= 3 ? rows.get(0).size() - 2 : -1; // Second to last
            balanceCol = rows.get(0).size() >= 2 ? rows.get(0).size() - 1 : -1; // Last column
        }
        
        logger.debug("Detected columns - Date: {}, Desc: {}, Amount: {}, Balance: {}", 
                    dateCol, descCol, amountCol, balanceCol);
        
        // Parse data rows
        for (List<RectangularTextContainer> row : rows) {
            if (row.size() <= dateCol || row.size() <= descCol) {
                continue;
            }
            
            try {
                String dateStr = row.get(dateCol).getText().trim();
                String description = row.get(descCol).getText().trim();
                
                // Skip empty rows or header rows
                if (dateStr.isEmpty() || description.isEmpty() || 
                    description.toLowerCase().contains("description") ||
                    dateStr.toLowerCase().contains("date")) {
                    continue;
                }
                
                // Try to parse as date to validate
                LocalDate date = parseDate(dateStr, COMMON_DATE_FORMATS);
                
                // Get amount and balance
                double amount = 0.0;
                double balance = 0.0;
                
                if (amountCol != -1 && amountCol < row.size()) {
                    String amountStr = row.get(amountCol).getText().trim();
                    if (!amountStr.isEmpty()) {
                        amount = parseAmount(amountStr);
                    }
                }
                
                if (balanceCol != -1 && balanceCol < row.size()) {
                    String balanceStr = row.get(balanceCol).getText().trim();
                    if (!balanceStr.isEmpty()) {
                        balance = parseAmount(balanceStr);
                    }
                }
                
                BankTransaction transaction = new BankTransaction();
                transaction.setDate(date);
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setBalance(balance);
                transaction.setType(amount >= 0 ? "CREDIT" : "DEBIT");
                
                transactions.add(transaction);
                
                logger.debug("Extracted transaction: {} | {} | R{} | R{}", 
                           date, description, amount, balance);
                
            } catch (Exception e) {
                logger.debug("Failed to parse table row: {}", e.getMessage());
            }
        }
        
        return transactions;
    }
    
    /**
     * Enhanced method to handle different PDF structures and layouts
     */
    public List<BankTransaction> extractTransactionsFromPdfAdvanced(MultipartFile pdfFile) throws IOException {
        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        
        logger.info("Starting advanced bank statement extraction from PDF: {}", pdfFile.getOriginalFilename());
        
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            List<BankTransaction> allTransactions = new ArrayList<>();
            
            // Try extracting from each page separately (some banks split across pages)
            for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                
                String pageText = stripper.getText(document);
                logger.debug("Extracting from page {}: {} characters", pageNum, pageText.length());
                
                List<BankTransaction> pageTransactions = extractTransactionsFromText(pageText);
                allTransactions.addAll(pageTransactions);
            }
            
            // Remove duplicates that might appear across pages
            return removeDuplicateTransactions(allTransactions);
        }
    }
    
    /**
     * Remove duplicate transactions that might appear across multiple pages
     */
    private List<BankTransaction> removeDuplicateTransactions(List<BankTransaction> transactions) {
        Set<String> seen = new HashSet<>();
        List<BankTransaction> unique = new ArrayList<>();
        
        for (BankTransaction transaction : transactions) {
            String key = transaction.getDate() + "|" + 
                        transaction.getDescription() + "|" + 
                        String.format("%.2f", transaction.getAmount());
            
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(transaction);
            }
        }
        
        logger.info("Removed {} duplicate transactions", transactions.size() - unique.size());
        return unique;
    }
    
    /**
     * Attempt to extract transactions using OCR if text extraction fails
     */
    public List<BankTransaction> extractWithOCRFallback(MultipartFile pdfFile) throws IOException {
        // First try normal text extraction
        List<BankTransaction> transactions = extractTransactionsFromPdf(pdfFile);
        
        if (!transactions.isEmpty()) {
            return transactions;
        }
        

        
        // For now, return empty list with recommendation
        // In future: integrate with OCR service like Tesseract
        return transactions;
    }

    /**
     * Extract transactions from text content with auto-detection of bank format
     */
    public List<BankTransaction> extractTransactionsFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text content is required");
        }
        
        String cleanedText = cleanText(text);
        String detectedBank = detectBankType(cleanedText);
        
        logger.info("Detected bank type: {}", detectedBank);
        logger.debug("First 500 chars of cleaned text: {}", cleanedText.substring(0, Math.min(500, cleanedText.length())));
        
        List<BankTransaction> transactions = new ArrayList<>();
        
        // Try bank-specific patterns first
        if (detectedBank != null && BANK_PATTERNS.containsKey(detectedBank)) {
            transactions = extractWithBankPattern(cleanedText, BANK_PATTERNS.get(detectedBank));
        }
        
        // If bank-specific extraction failed, try fallback patterns
        if (transactions.isEmpty()) {
            logger.info("Bank-specific extraction failed, trying fallback patterns");
            transactions = extractWithFallbackPatterns(cleanedText);
        }
        
        // Clean and validate transactions
        transactions = cleanAndValidateTransactions(transactions);
        
        logger.info("Successfully extracted {} transactions", transactions.size());
        
        // Log date range for debugging
        if (!transactions.isEmpty()) {
            LocalDate minDate = transactions.stream().map(BankTransaction::getDate).min(LocalDate::compareTo).orElse(null);
            LocalDate maxDate = transactions.stream().map(BankTransaction::getDate).max(LocalDate::compareTo).orElse(null);
            logger.info("Transaction date range: {} to {}", minDate, maxDate);
        }
        
        return transactions;
    }
    
    /**
     * Detect bank type from statement content
     */
    private String detectBankType(String text) {
        String lowerText = text.toLowerCase();
        
        // Check for bank-specific indicators with enhanced FNB detection
        if (lowerText.contains("fnb") || 
            lowerText.contains("first national bank") ||
            lowerText.contains("firstrand") ||
            lowerText.contains("first rand") ||
            lowerText.contains("fnb bank") ||
            lowerText.contains("first national") ||
            lowerText.matches(".*\\bfnb\\b.*") ||
            lowerText.matches(".*\\bfirst\\s+national\\s+bank\\b.*")) {
            return "FNB";
        } else if (lowerText.contains("standard bank") || 
                   lowerText.contains("stanbic") ||
                   lowerText.contains("standard chartered") ||
                   lowerText.contains("sbsa")) {
            return "STANDARD_BANK";
        } else if (lowerText.contains("absa") || 
                   lowerText.contains("amalgamated banks") ||
                   lowerText.contains("amalgamated bank") ||
                   lowerText.contains("barclays africa")) {
            return "ABSA";
        } else if (lowerText.contains("nedbank") || 
                   lowerText.contains("ned bank") ||
                   lowerText.contains("nedcor") ||
                   lowerText.contains("ned group")) {
            return "NEDBANK";
        } else if (lowerText.contains("capitec") || 
                   lowerText.contains("capitec bank") ||
                   lowerText.contains("cap bank")) {
            return "CAPITEC";
        }
        
        return null; // Unknown bank, will use fallback patterns
    }
    
    /**
     * Extract transactions using bank-specific patterns
     */
    private List<BankTransaction> extractWithBankPattern(String text, BankPatterns patterns) {
        List<BankTransaction> transactions = new ArrayList<>();
        Matcher matcher = patterns.transactionPattern.matcher(text);
        
        while (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                String description = matcher.group(2).trim();
                String amountStr = matcher.group(3);
                String balanceStr = matcher.group(4);
                
                LocalDate date = parseDate(dateStr, patterns.dateFormats);
                double amount = parseAmount(amountStr);
                double balance = parseAmount(balanceStr);
                
                BankTransaction transaction = new BankTransaction();
                transaction.setDate(date);
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setBalance(balance);
                transaction.setType(amount >= 0 ? "CREDIT" : "DEBIT");
                
                transactions.add(transaction);
                
            } catch (Exception e) {
                logger.warn("Failed to parse transaction line: {}", matcher.group(0), e);
            }
        }
        
        return transactions;
    }
    
    /**
     * Extract transactions using fallback patterns when bank type is unknown
     */
    private List<BankTransaction> extractWithFallbackPatterns(String text) {
        List<BankTransaction> transactions = new ArrayList<>();
        
        // Try single-line patterns first
        for (Pattern pattern : FALLBACK_PATTERNS) {
            transactions = extractWithPattern(text, pattern);
            if (!transactions.isEmpty()) {
                logger.info("Successfully extracted transactions with fallback pattern");
                break;
            }
        }
        
        // If single-line patterns failed, try multi-line table format parsing
        if (transactions.isEmpty()) {
            logger.info("Single-line patterns failed, trying multi-line table format parsing");
            transactions = extractMultiLineTableFormat(text);
            if (!transactions.isEmpty()) {
                logger.info("Successfully extracted {} transactions with multi-line table format", transactions.size());
            }
        }
        
        return transactions;
    }
    
    /**
     * Extract transactions using a specific pattern
     */
    private List<BankTransaction> extractWithPattern(String text, Pattern pattern) {
        List<BankTransaction> transactions = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                String description = matcher.group(2).trim();
                String amountStr = matcher.group(3);
                String balanceStr = matcher.group(4);
                
                LocalDate date = parseDate(dateStr, COMMON_DATE_FORMATS);
                double amount = parseAmount(amountStr);
                double balance = parseAmount(balanceStr);
                
                BankTransaction transaction = new BankTransaction();
                transaction.setDate(date);
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setBalance(balance);
                transaction.setType(amount >= 0 ? "CREDIT" : "DEBIT");
                
                transactions.add(transaction);
                
            } catch (Exception e) {
                logger.debug("Failed to parse transaction line: {}", matcher.group(0));
            }
        }
        
        return transactions;
    }
    
    /**
     * Extract transactions from multi-line table format where each field is on a separate line
     * Format:
     * 2024-08-01
     * SALARY PAYMENT - CORPORATION GHI
     * +35,000.00
     * 48,500.00
     */
    private List<BankTransaction> extractMultiLineTableFormat(String text) {
        List<BankTransaction> transactions = new ArrayList<>();
        
        // Split text into lines and process sequentially
        String[] lines = text.split("\\n");
        
        // Patterns for each component
        Pattern datePattern = Pattern.compile("^\\s*(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})\\s*$");
        Pattern amountPattern = Pattern.compile("^\\s*([+-]?R?\\s*[\\d,]+\\.\\d{2})\\s*$");
        
        int i = 0;
        while (i < lines.length) {
            String line = lines[i].trim();
            
            // Look for a date line
            Matcher dateMatcher = datePattern.matcher(line);
            if (dateMatcher.matches() && i + 3 < lines.length) {
                try {
                    String dateStr = dateMatcher.group(1);
                    
                    // Next line should be description
                    i++;
                    String description = lines[i].trim();
                    
                    // Skip if description is empty or looks like a header
                    if (description.isEmpty() || 
                        description.equalsIgnoreCase("description") ||
                        description.equalsIgnoreCase("amount") ||
                        description.equalsIgnoreCase("balance") ||
                        description.equalsIgnoreCase("date")) {
                        continue;
                    }
                    
                    // Next line should be amount
                    i++;
                    String amountLine = lines[i].trim();
                    Matcher amountMatcher = amountPattern.matcher(amountLine);
                    if (!amountMatcher.matches()) {
                        // Maybe amount and balance are on same line, or format is different
                        continue;
                    }
                    String amountStr = amountMatcher.group(1);
                    
                    // Next line should be balance
                    i++;
                    String balanceLine = lines[i].trim();
                    Matcher balanceMatcher = amountPattern.matcher(balanceLine);
                    if (!balanceMatcher.matches()) {
                        continue;
                    }
                    String balanceStr = balanceMatcher.group(1);
                    
                    // Parse the transaction
                    LocalDate date = parseDate(dateStr, COMMON_DATE_FORMATS);
                    double amount = parseAmount(amountStr);
                    double balance = parseAmount(balanceStr);
                    
                    BankTransaction transaction = new BankTransaction();
                    transaction.setDate(date);
                    transaction.setDescription(description);
                    transaction.setAmount(amount);
                    transaction.setBalance(balance);
                    transaction.setType(amount >= 0 ? "CREDIT" : "DEBIT");
                    
                    transactions.add(transaction);
                    
                    logger.debug("Parsed multi-line transaction: {} | {} | R{} | R{}",
                               date, description, amount, balance);
                    
                } catch (Exception e) {
                    logger.debug("Failed to parse multi-line transaction starting at line {}: {}", i, e.getMessage());
                }
            }
            i++;
        }
        
        return transactions;
    }
    
    /**
     * Parse date string using multiple format attempts with intelligent 2-digit year handling
     */
    private LocalDate parseDate(String dateStr, String[] formats) {
        dateStr = dateStr.trim();
        
        // Try bank-specific formats first
        for (String format : formats) {
            LocalDate parsed = tryParseDate(dateStr, format);
            if (parsed != null) {
                return adjustYearIfNeeded(parsed, dateStr);
            }
        }
        
        // Try common formats as fallback
        for (String format : COMMON_DATE_FORMATS) {
            LocalDate parsed = tryParseDate(dateStr, format);
            if (parsed != null) {
                return adjustYearIfNeeded(parsed, dateStr);
            }
        }
        
        logger.warn("Unable to parse date: {}", dateStr);
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }
    
    /**
     * Try to parse a date with a specific format
     */
    private LocalDate tryParseDate(String dateStr, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Adjust year if it seems incorrect (e.g., 2-digit year parsed as early 1900s/2000s)
     * Bank statements should be recent, so dates from 1900s-early 2000s are likely parsing errors
     */
    private LocalDate adjustYearIfNeeded(LocalDate date, String originalDateStr) {
        int year = date.getYear();
        int currentYear = LocalDate.now().getYear();
        
        // If the year is suspiciously old (before 2010), it's likely a 2-digit year parsing issue
        // Check if original string contains a 2-digit year pattern
        boolean hasTwoDigitYear = originalDateStr.matches(".*\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2}\\b.*") ||
                                 originalDateStr.matches(".*\\b\\d{2}[/-]\\d{1,2}[/-]\\d{1,2}\\b.*");
        
        if (year < 2010 && hasTwoDigitYear) {
            // Assume it's a 2-digit year that should be in 2000s or 2010s
            int twoDigitYear = year % 100;
            
            // If parsed year is 00-25 (e.g., 2001 -> 01), map to 2000-2025
            // If parsed year is 26-99 (would be parsed as 1926-1999), map to 2026-2099
            int adjustedYear;
            if (year < 2026) {
                // Already in 2000-2025 range, but came from 2-digit parsing
                adjustedYear = 2000 + twoDigitYear;
            } else {
                // Was parsed as 19xx, adjust to 20xx
                adjustedYear = 2000 + twoDigitYear;
            }
            
            // Don't allow future dates beyond current year + 1
            if (adjustedYear > currentYear + 1) {
                // If it would be in the future, it's probably from last century
                adjustedYear = 1900 + twoDigitYear;
            }
            
            LocalDate adjustedDate = LocalDate.of(adjustedYear, date.getMonth(), date.getDayOfMonth());
            logger.debug("Adjusted date from {} (year {}) to {} based on 2-digit year pattern", 
                        date, year, adjustedDate);
            return adjustedDate;
        }
        
        // For dates that seem reasonable (2010 or later), or 4-digit years, return as-is
        return date;
    }
    
    /**
     * Parse amount string and handle various formats
     */
    private double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }
        
        // Clean amount string
        String cleaned = amountStr.trim()
            .replaceAll("R", "")           // Remove currency symbol
            .replaceAll("\\s+", "")        // Remove spaces
            .replaceAll(",", "");          // Remove thousands separators
        
        // Handle negative amounts in parentheses
        boolean isNegative = cleaned.startsWith("(") && cleaned.endsWith(")");
        if (isNegative) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        // Handle explicit negative sign
        if (cleaned.startsWith("-")) {
            isNegative = true;
            cleaned = cleaned.substring(1);
        }
        
        try {
            double amount = Double.parseDouble(cleaned);
            return isNegative ? -amount : amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse amount: " + amountStr);
        }
    }
    
    /**
     * Clean and validate extracted transactions
     */
    private List<BankTransaction> cleanAndValidateTransactions(List<BankTransaction> transactions) {
        return transactions.stream()
            .filter(this::isValidTransaction)
            .sorted(Comparator.comparing(BankTransaction::getDate))
            .distinct() // Remove duplicates based on date, description, and amount
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Validate if a transaction is valid
     */
    private boolean isValidTransaction(BankTransaction transaction) {
        return transaction.getDate() != null
            && transaction.getDescription() != null
            && !transaction.getDescription().trim().isEmpty()
            && transaction.getDescription().length() > 3
            && Math.abs(transaction.getAmount()) > 0.01; // Ignore tiny amounts
    }
    
    /**
     * Clean text content for better parsing
     */
    private String cleanText(String text) {
        return text
            .replaceAll("\\r\\n", "\n")     // Normalize line endings
            .replaceAll("\\r", "\n")        // Normalize line endings
            .replaceAll("\\t", " ")         // Replace tabs with spaces
            .replaceAll(" {2,}", " ")       // Replace multiple spaces with single space
            .trim();
    }
    
    /**
     * Analyze statement to determine quality and provide extraction statistics
     */
    public BankStatementAnalysisResult analyzeExtractionQuality(List<BankTransaction> transactions, String originalText) {
        BankStatementAnalysisResult result = new BankStatementAnalysisResult();
        
        result.setTotalTransactionsExtracted(transactions.size());
        result.setDateRange(getDateRange(transactions));
        result.setQualityScore(calculateQualityScore(transactions, originalText));
        result.setWarnings(generateExtractionWarnings(transactions));
        result.setRecommendations(generateRecommendations(transactions));
        
        return result;
    }
    
    private Map<String, LocalDate> getDateRange(List<BankTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Map.of();
        }
        
        LocalDate minDate = transactions.stream()
            .map(BankTransaction::getDate)
            .min(LocalDate::compareTo)
            .orElse(null);
            
        LocalDate maxDate = transactions.stream()
            .map(BankTransaction::getDate)
            .max(LocalDate::compareTo)
            .orElse(null);
            
        return Map.of("start", minDate, "end", maxDate);
    }
    
    private double calculateQualityScore(List<BankTransaction> transactions, String originalText) {
        double score = 0.0;
        
        // Base score for having transactions
        if (!transactions.isEmpty()) {
            score += 30.0;
        }
        
        // Score for sufficient transaction count
        if (transactions.size() >= 50) {
            score += 25.0;
        } else if (transactions.size() >= 20) {
            score += 15.0;
        }
        
        // Score for date range coverage
        Map<String, LocalDate> dateRange = getDateRange(transactions);
        if (!dateRange.isEmpty()) {
            LocalDate start = dateRange.get("start");
            LocalDate end = dateRange.get("end");
            long daysBetween = start.until(end).getDays();
            
            if (daysBetween >= 90) {
                score += 25.0;
            } else if (daysBetween >= 30) {
                score += 15.0;
            }
        }
        
        // Score for transaction variety
        long uniqueDescriptions = transactions.stream()
            .map(BankTransaction::getDescription)
            .distinct()
            .count();
            
        if (uniqueDescriptions > transactions.size() * 0.7) {
            score += 20.0;
        } else if (uniqueDescriptions > transactions.size() * 0.5) {
            score += 10.0;
        }
        
        return Math.min(100.0, score);
    }
    
    private List<String> generateExtractionWarnings(List<BankTransaction> transactions) {
        List<String> warnings = new ArrayList<>();
        
        if (transactions.size() < 20) {
            warnings.add("Insufficient transactions for reliable analysis (minimum 50 recommended)");
        }
        
        Map<String, LocalDate> dateRange = getDateRange(transactions);
        if (!dateRange.isEmpty()) {
            LocalDate start = dateRange.get("start");
            LocalDate end = dateRange.get("end");
            long daysBetween = start.until(end).getDays();
            
            if (daysBetween < 30) {
                warnings.add("Short transaction history (minimum 3 months recommended)");
            }
        }
        
        long duplicateCount = transactions.size() - transactions.stream()
            .map(t -> t.getDate() + "|" + t.getDescription() + "|" + t.getAmount())
            .distinct()
            .count();
            
        if (duplicateCount > 0) {
            warnings.add("Possible duplicate transactions detected: " + duplicateCount);
        }
        
        return warnings;
    }
    
    private List<String> generateRecommendations(List<BankTransaction> transactions) {
        List<String> recommendations = new ArrayList<>();
        
        if (transactions.size() < 50) {
            recommendations.add("Upload a statement with at least 3 months of transaction history");
        }
        
        recommendations.add("Ensure the PDF is clear and text is not scanned as images");
        recommendations.add("Use official bank statements rather than screenshots");
        
        return recommendations;
    }
    
    // Helper classes
    private static class BankPatterns {
        final Pattern transactionPattern;
        final String[] dateFormats;
        final String bankName;
        
        BankPatterns(Pattern transactionPattern, String[] dateFormats, String bankName) {
            this.transactionPattern = transactionPattern;
            this.dateFormats = dateFormats;
            this.bankName = bankName;
        }
    }
    
    public static class BankStatementAnalysisResult {
        private int totalTransactionsExtracted;
        private Map<String, LocalDate> dateRange;
        private double qualityScore;
        private List<String> warnings;
        private List<String> recommendations;
        
        // Getters and setters
        public int getTotalTransactionsExtracted() { return totalTransactionsExtracted; }
        public void setTotalTransactionsExtracted(int totalTransactionsExtracted) { this.totalTransactionsExtracted = totalTransactionsExtracted; }
        
        public Map<String, LocalDate> getDateRange() { return dateRange; }
        public void setDateRange(Map<String, LocalDate> dateRange) { this.dateRange = dateRange; }
        
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
}
