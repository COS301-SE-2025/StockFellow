package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateBankDetailRequest;
import com.stockfellow.transactionservice.dto.BankDetailResponse;
import com.stockfellow.transactionservice.model.BankDetails;
import com.stockfellow.transactionservice.repository.BankDetailRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankDetailService {

    private static final Logger logger = LoggerFactory.getLogger(BankDetailService.class);
    private static final int MAX_BANK_DETAILS_PER_USER = 5;

    private final BankDetailRepository bankDetailRepository;

    @Autowired
    public BankDetailService(BankDetailRepository bankDetailRepository) {
        this.bankDetailRepository = bankDetailRepository;
    }

    /**
     * Add new bank details for a user
     */
    public BankDetails addBankDetails(CreateBankDetailRequest request) {
        logger.info("Adding bank details for user: {}", request.getUserId());

        // Validate request
        validateCreateRequest(request);

        // Check if user has reached the maximum limit
        long existingCount = bankDetailRepository.countByUserIdAndIsActiveTrue(request.getUserId());
        if (existingCount >= MAX_BANK_DETAILS_PER_USER) {
            throw new IllegalStateException("User has reached maximum limit of " + MAX_BANK_DETAILS_PER_USER + " bank details");
        }

        // Check for duplicate card number (last 4 digits)
        String last4Digits = extractLast4Digits(request.getCardNumber());
        List<BankDetails> existingWithSameLast4 = bankDetailRepository
                .findByUserIdAndLastFourDigits(request.getUserId(), last4Digits);
        
        if (!existingWithSameLast4.isEmpty()) {
            logger.warn("User {} already has a card ending in {}", request.getUserId(), last4Digits);
            // You might want to allow this or throw an exception based on business rules
        }

        // Create new bank details entity
        BankDetails bankDetails = new BankDetails();
        bankDetails.setId(UUID.randomUUID());
        bankDetails.setUserId(request.getUserId());
        bankDetails.setBank(request.getBank());
        bankDetails.setCardNumber(encryptCardNumber(request.getCardNumber())); // Should be encrypted
        bankDetails.setCardHolder(request.getCardHolder());
        bankDetails.setExpiryMonth(request.getExpiryMonth());
        bankDetails.setExpiryYear(request.getExpiryYear());
        bankDetails.setCardType(request.getCardType());
        bankDetails.setCreatedAt(LocalDateTime.now());
        bankDetails.setUpdatedAt(LocalDateTime.now());

        // Determine if this should be active
        boolean hasExistingActive = bankDetailRepository.existsByUserIdAndIsActiveTrue(request.getUserId());
        boolean shouldBeActive = request.getSetAsActive() || !hasExistingActive;

        if (shouldBeActive && hasExistingActive) {
            // Deactivate all other bank details for this user
            deactivateAllBankDetailsForUser(request.getUserId());
        }

        bankDetails.setIsActive(shouldBeActive);

        // Save the bank details
        BankDetails savedBankDetails = bankDetailRepository.save(bankDetails);
        logger.info("Successfully added bank details with ID: {} for user: {}", 
                   savedBankDetails.getId(), request.getUserId());

        return savedBankDetails;
    }

    /**
     * Get all bank details for a user
     */
    @Transactional(readOnly = true)
    public List<BankDetailResponse> getUserBankDetails(UUID userId) {
        logger.info("Fetching all bank details for user: {}", userId);

        List<BankDetails> bankDetailsList = bankDetailRepository.findByUserId(userId);
        
        return bankDetailsList.stream()
                .map(BankDetailResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get active bank details for a user (for transactions)
     */
    @Transactional(readOnly = true)
    public BankDetailResponse getActiveBankDetails(UUID userId) {
        logger.info("Fetching active bank details for user: {}", userId);

        Optional<BankDetails> activeBankDetails = 
                bankDetailRepository.findFirstByUserIdAndIsActiveTrueOrderByCreatedAtAsc(userId);

        if (activeBankDetails.isEmpty()) {
            throw new IllegalArgumentException("No active bank details found for user: " + userId);
        }

        return BankDetailResponse.from(activeBankDetails.get());
    }

    /**
     * Get specific bank details by ID with user validation
     */
    @Transactional(readOnly = true)
    public BankDetailResponse getBankDetailsById(UUID bankDetailsId, UUID userId) {
        logger.info("Fetching bank details by ID: {} for user: {}", bankDetailsId, userId);

        Optional<BankDetails> bankDetails = bankDetailRepository.findByIdAndUserId(bankDetailsId, userId);
        
        if (bankDetails.isEmpty()) {
            throw new IllegalArgumentException("Bank details not found or access denied");
        }

        return BankDetailResponse.from(bankDetails.get());
    }

    /**
     * Activate specific bank details (set as primary)
     */
    public BankDetailResponse activateBankDetails(UUID bankDetailsId, UUID userId) {
        logger.info("Activating bank details: {} for user: {}", bankDetailsId, userId);

        // Find the bank details
        Optional<BankDetails> optionalBankDetails = bankDetailRepository.findByIdAndUserId(bankDetailsId, userId);
        if (optionalBankDetails.isEmpty()) {
            throw new IllegalArgumentException("Bank details not found or access denied");
        }

        BankDetails bankDetails = optionalBankDetails.get();

        // If already active, no need to change
        if (bankDetails.getIsActive()) {
            logger.info("Bank details {} is already active", bankDetailsId);
            return BankDetailResponse.from(bankDetails);
        }

        // Deactivate all other bank details for this user
        deactivateAllBankDetailsForUser(userId);

        // Activate the specified bank details
        bankDetails.setIsActive(true);
        bankDetails.setUpdatedAt(LocalDateTime.now());
        
        BankDetails savedBankDetails = bankDetailRepository.save(bankDetails);
        logger.info("Successfully activated bank details: {}", bankDetailsId);

        return BankDetailResponse.from(savedBankDetails);
    }

    /**
     * Deactivate specific bank details
     */
    public void deactivateBankDetails(UUID bankDetailsId, UUID userId) {
        logger.info("Deactivating bank details: {} for user: {}", bankDetailsId, userId);

        // Find the bank details
        Optional<BankDetails> optionalBankDetails = bankDetailRepository.findByIdAndUserId(bankDetailsId, userId);
        if (optionalBankDetails.isEmpty()) {
            throw new IllegalArgumentException("Bank details not found or access denied");
        }

        BankDetails bankDetails = optionalBankDetails.get();

        // Check if this is the user's only active bank detail
        long activeCount = bankDetailRepository.countByUserIdAndIsActiveTrue(userId);
        if (activeCount <= 1 && bankDetails.getIsActive()) {
            throw new IllegalStateException("Cannot deactivate the only active bank details for user");
        }

        // Deactivate the bank details
        bankDetails.setIsActive(false);
        bankDetails.setUpdatedAt(LocalDateTime.now());
        
        bankDetailRepository.save(bankDetails);
        logger.info("Successfully deactivated bank details: {}", bankDetailsId);
    }

    /**
     * Delete bank details (hard delete)
     */
    public void deleteBankDetails(UUID bankDetailsId, UUID userId) {
        logger.info("Deleting bank details: {} for user: {}", bankDetailsId, userId);

        // Find the bank details
        Optional<BankDetails> optionalBankDetails = bankDetailRepository.findByIdAndUserId(bankDetailsId, userId);
        if (optionalBankDetails.isEmpty()) {
            throw new IllegalArgumentException("Bank details not found or access denied");
        }

        BankDetails bankDetails = optionalBankDetails.get();

        // Check if this is the user's only active bank detail
        long activeCount = bankDetailRepository.countByUserIdAndIsActiveTrue(userId);
        if (activeCount <= 1 && bankDetails.getIsActive()) {
            throw new IllegalStateException("Cannot delete the only active bank details for user");
        }

        // Delete the bank details
        bankDetailRepository.delete(bankDetails);
        logger.info("Successfully deleted bank details: {}", bankDetailsId);
    }

    /**
     * Get count of active bank details for a user
     */
    @Transactional(readOnly = true)
    public long getBankDetailsCount(UUID userId) {
        return bankDetailRepository.countByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Check if user has any active bank details
     */
    @Transactional(readOnly = true)
    public boolean hasActiveBankDetails(UUID userId) {
        return bankDetailRepository.existsByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get expiring bank details (useful for notifications)
     */
    @Transactional(readOnly = true)
    public List<BankDetailResponse> getExpiringBankDetails(UUID userId, int year, int month) {
        List<BankDetails> expiringCards = bankDetailRepository.findExpiringCards(userId, year, month);
        return expiringCards.stream()
                .map(BankDetailResponse::from)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private void validateCreateRequest(CreateBankDetailRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (request.getExpiryYear() != null && request.getExpiryMonth() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (request.getExpiryYear() < now.getYear() || 
                (request.getExpiryYear().equals(now.getYear()) && request.getExpiryMonth() < now.getMonthValue())) {
                throw new IllegalArgumentException("Card expiry date cannot be in the past");
            }
        }
    }

    private void deactivateAllBankDetailsForUser(UUID userId) {
        List<BankDetails> activeBankDetails = bankDetailRepository.findByUserIdAndIsActiveTrue(userId);
        for (BankDetails bd : activeBankDetails) {
            bd.setIsActive(false);
            bd.setUpdatedAt(LocalDateTime.now());
        }
        if (!activeBankDetails.isEmpty()) {
            bankDetailRepository.saveAll(activeBankDetails);
            logger.info("Deactivated {} existing bank details for user: {}", activeBankDetails.size(), userId);
        }
    }

    private String extractLast4Digits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Encrypt card number - IMPLEMENT THIS WITH PROPER ENCRYPTION
     * This is a placeholder - you should use proper encryption like AES
     */
    private String encryptCardNumber(String cardNumber) {
        // TODO: Implement proper encryption
        // For now, this is a placeholder that just returns the card number
        // In production, you should:
        // 1. Use a proper encryption library (AES-256)
        // 2. Store the encryption key securely (e.g., AWS KMS, Azure Key Vault)
        // 3. Consider using payment vaults instead (Stripe, PayPal, etc.)
        
        logger.warn("Card number encryption not implemented - using plaintext (SECURITY RISK)");
        return cardNumber;
    }

    /**
     * Decrypt card number - IMPLEMENT THIS WITH PROPER DECRYPTION
     */
    private String decryptCardNumber(String encryptedCardNumber) {
        // TODO: Implement proper decryption
        // This should match the encryption method above
        
        return encryptedCardNumber;
    }
}