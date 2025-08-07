package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.model.User;
import com.stockfellow.transactionservice.model.PayerDetails;
import com.stockfellow.transactionservice.model.PayoutDetails;
import com.stockfellow.transactionservice.dto.CreatePayerDetailsDto;
import com.stockfellow.transactionservice.dto.CreatePayoutDetailsDto;
import com.stockfellow.transactionservice.dto.InitializeCardAuthDto;
import com.stockfellow.transactionservice.integration.dto.PaystackAuthorization;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionResponse;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionVerificationResponse;
import com.stockfellow.transactionservice.repository.UserRepository;
import com.stockfellow.transactionservice.repository.PayerDetailsRepository;
import com.stockfellow.transactionservice.repository.PayoutDetailsRepository;
import com.stockfellow.transactionservice.integration.PaystackService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayoutDetailsRepository payoutDetailsRepository;

    @Autowired
    private PayerDetailsRepository payerDetailsRepository;

    @Autowired
    private PaystackService paystackService;

    /**
     * ===================================================
     * =                 PayerDetails                    =
     * ===================================================
     */
    public PayerDetails addPayerDetails(CreatePayerDetailsDto createDto){
        logger.info("Adding payer details for user: {}", createDto.getUserId());

        //Validate user exists
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + createDto.getUserId()));

        //Check if these details already exist
        validateNoDuplicatePayerDetails(createDto);
            
        //Check if this needs to be set as active
        boolean shouldBeActive = shouldSetAsActive(createDto.getUserId());

        PayerDetails pd = new PayerDetails(createDto.getUserId(), createDto.getType(), createDto.getEmail());
        pd.setAuthCode(createDto.getAuthCode());
        pd.setCardType(createDto.getCardType());
        pd.setLast4(createDto.getLast4());
        pd.setExpMonth(createDto.getExpMonth());
        pd.setExpYear(createDto.getExpYear());
        pd.setBin(createDto.getBin());
        pd.setBank(createDto.getBank());
        pd.setSignature(createDto.getSignature());
        pd.setIsActive(shouldBeActive);
        // Chanel?
        //Reusable?
        //Countrycode?
        //Account name?

        pd = payerDetailsRepository.save(pd);

        logger.info("Payer details added successfully with ID: {}", pd.getPayerId());
        return pd;
    }

    public List<PayerDetails> findPayerDetailsByUserId(UUID userId){
        logger.info("Searching for payer details for user: {}", userId);

        List<PayerDetails> payerDetailsList = payerDetailsRepository.findByUserId(userId);

        logger.info("Found {} payer details for user: {}", payerDetailsList.size(), userId);
        return payerDetailsList;
    }

    //TODO: How to handle this with paystack?
    // public PayerDetails updatePayerDetails(UUID payerId, UpdatePayerDetailsDto updateDto){
    //     logger.info("Updating payer detailf for payer details with ID: {}", payerId);

    //     PayerDetails pd = payerDetailsRepository.findById(payerId)
    //         .orElseThrow(() -> new RuntimeException("Cannot update payer details with ID: " + payerId));

        
    // }

    public PayerDetails deactivateCard(UUID payerId) {
        logger.info("Deactivating card with payer details ID: {}", payerId);

        PayerDetails userCard = payerDetailsRepository.findById(payerId)
            .orElseThrow(() -> new RuntimeException("Payer details not found with ID: " + payerId));
        
        userCard.setIsActive(false);
        userCard = payerDetailsRepository.save(userCard);

        return userCard;

    }

    public Map<String,Object> initializeCardAuth(InitializeCardAuthDto initDto){
        try {
            // Create Paystack transaction request for card capture
            PaystackTransactionRequest request = new PaystackTransactionRequest();
            request.setEmail(initDto.getEmail());
            request.setAmount(100); // R1.00 in Rands (minimal amount for card auth)
            request.setReference(generateAuthReference());
            request.setCallbackUrl("http://localhost:4080/api/payment-methods/payer/callback");
            
            // Add metadata to identify this as card authorization
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("purpose", "card_authorization");
            metadata.put("user_id", initDto.getUserId().toString());
            metadata.put("payment_method_type", initDto.getType().toString());
            request.setMetadata(metadata);

            PaystackTransactionResponse response = paystackService.initializeTransaction(request);
            
            if (response.getStatus()) {
                // Store pending authorization details
                PayerDetails pendingAuth = new PayerDetails();
                pendingAuth.setUserId(initDto.getUserId());
                pendingAuth.setType(initDto.getType());
                pendingAuth.setEmail(initDto.getEmail());
                pendingAuth.setIsAuthenticated(false);
                pendingAuth.setIsActive(false);
                pendingAuth.setPaystackReference(response.getData().getReference());
                
                payerDetailsRepository.save(pendingAuth);
                
                return Map.of(
                    "status", true,
                    "message", "Authorization initialized successfully",
                    "data", Map.of(
                        "authorization_url", response.getData().getAuthorizationUrl(),
                        "access_code", response.getData().getAccessCode(),
                        "reference", response.getData().getReference()
                    )
                );
            } else {
                throw new RuntimeException("Paystack initialization failed: " + response.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize card authorization", e);
            return Map.of(
                "status", false,
                "message", "Failed to initialize card authorization: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> processPaystackCallback(String reference, String trxref) {
        try {
            PaystackTransactionVerificationResponse verification = paystackService.verifyTransaction(reference);
            
            if (verification.getStatus() && "success".equals(verification.getData().getStatus())) {
                
                // Extract card authorization details
                PaystackAuthorization authorization = verification.getData().getAuthorization();
                User customer = verification.getData().getCustomer();
                
                if (authorization == null || customer == null) {
                    return Map.of(
                        "status", false,
                        "message", "Invalid authorization data received from Paystack"
                    );
                }

                // Create PayerDetails with captured card information
                CreatePayerDetailsDto createDto = new CreatePayerDetailsDto();
                createDto.setUserId(UUID.fromString(
                    verification.getData().getMetadata().get("user_id").toString()));
                createDto.setType(PayerDetails.PaymentMethodType.valueOf(
                    verification.getData().getMetadata().get("payment_method_type").toString()));
                createDto.setEmail(customer.getEmail());
                createDto.setAuthCode(authorization.getAuthorizationCode());
                createDto.setCardType(authorization.getCardType());
                createDto.setLast4(authorization.getLast4());
                createDto.setExpMonth(authorization.getExpMonth());
                createDto.setExpYear(authorization.getExpYear());
                createDto.setBin(authorization.getBin());
                createDto.setBank(authorization.getBank());
                createDto.setSignature(authorization.getSignature());
                
                PayerDetails savedCard = this.addPayerDetails(createDto);
                
                return Map.of(
                    "status", true,
                    "message", "Card authorized and saved successfully", // Fixed typo
                    "data", Map.of(
                        "card_type", authorization.getCardType(),
                        "last4", authorization.getLast4(),
                        "bank", authorization.getBank(),
                        "payer_id", savedCard.getPayerId()
                    )
                );
            } else {
                return Map.of(
                    "status", false,
                    "message", "Card authorization failed: " + 
                        (verification.getData() != null ? verification.getData().getMessage() : verification.getMessage())
                );
            }            
        } catch (Exception e) {
            logger.error("Failed to process card authorization callback", e); // Add logging
            return Map.of(
                "status", false,
                "message", "Failed to process card authorization: " + e.getMessage() // Fixed message
            );
        }
    }

    /**
     * ===================================================
     * =                 PayoutDetails                    =
     * ===================================================
     */
    public PayoutDetails addPayoutDetails(CreatePayoutDetailsDto createDto){
        logger.info("Adding payout details for user: {}", createDto.getUserId());

        //Validate user exists
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + createDto.getUserId()));

        validateNoDuplicatePayoutDetails(createDto.getUserId());
            
        PayoutDetails pd = new PayoutDetails(createDto.getUserId(), createDto.getType(), createDto.getRecipientName());
        pd.setAccountNumber(createDto.getAccountNumber());
        pd.setBankCode(createDto.getBankCode());
        pd.setBankName(createDto.getBankName());
        pd.setRecipientCode(createDto.getRecipientCode());
        //is default
        //is verified

        pd = payoutDetailsRepository.save(pd);

        logger.info("Payout details added successfully with ID: {}", pd.getPayoutId());
        return pd;

    }

    public List<PayoutDetails> findPayoutDetailsByUserId(UUID userId){
        logger.info("Searching for payout details for user: {}", userId);

        List<PayoutDetails> payoutDetailsList = payoutDetailsRepository.findByUserId(userId);

        logger.info("Found {} payout details for user: {}", payoutDetailsList.size(), userId);
        return payoutDetailsList;

    }

    //TODO: How to handle this with paystack?
    // public PayoutDetails updatePayoutDetails(UUID payoutId, UpdatePayoutDetailsDto updateDto){
    //     logger.info();

    // }

    // public PayoutDetails deletePayoutDetails(UUID payoutId) {
    //     logger.info();
    // }

    //Helpers
    public void validateNoDuplicatePayerDetails(CreatePayerDetailsDto createDto) {
        if (createDto.getSignature() != null) {
            boolean signatureExists = payerDetailsRepository.existsByUserIdAndSignature(createDto.getUserId(), createDto.getSignature());
            if (signatureExists) {
                throw new RuntimeException("This card is already saved to your account");
            }
        }
        
        if (createDto.getAuthCode() != null) {
            Optional<PayerDetails> existingAuth = payerDetailsRepository.findByAuthCode(createDto.getAuthCode());
            if (existingAuth.isPresent()) {
                throw new RuntimeException("Payment authorization already exists");
            }
        }
    }
    public void validateNoDuplicatePayoutDetails(UUID userId){
        Boolean pd = payoutDetailsRepository.existsByUserIdAndIsVerifiedTrue(userId);

        if (pd){
            throw new RuntimeException("User with ID: "+ userId + " already has payout details assigned");
        }
    }


    private boolean shouldSetAsActive(UUID userId) {
        long activeCardCount = payerDetailsRepository.countByUserIdAndIsActiveTrue(userId);
        return activeCardCount == 0;
    }

    private void deactivateOtherCards(UUID userId, UUID currentPayerId) {
        List<PayerDetails> userCards = payerDetailsRepository.findByUserIdAndIsActiveTrue(userId);
        userCards.stream()
            .filter(card -> !card.getPayerId().equals(currentPayerId))
            .forEach(card -> {
                card.setIsActive(false);
                payerDetailsRepository.save(card);
            });
    }

    private String generateAuthReference() {
        return "AUTH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

}
