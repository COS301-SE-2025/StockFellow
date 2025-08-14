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
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRecipientRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRecipientResponse;
import com.stockfellow.transactionservice.repository.UserRepository;
import com.stockfellow.transactionservice.repository.PayerDetailsRepository;
import com.stockfellow.transactionservice.repository.PayoutDetailsRepository;
import com.stockfellow.transactionservice.integration.PaystackService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${paystack.callback-base-url}")
    private String callbackBaseUrl;

    @Value("${paystack.secretKey}")
    private String paystackKey;


    /**
     * ===================================================
     * =                 Callback                        =
     * ===================================================
     */
    public Map<String, Object> processPaystackCallback(String reference) {
        try {
            // Just verify the transaction succeeded for user feedback
            PaystackTransactionVerificationResponse verification = paystackService.verifyTransaction(reference);
            
            if (verification.getStatus() && "success".equals(verification.getData().getStatus())) {
                return Map.of(
                    "status", true,
                    "message", "Payment completed successfully! We're processing your card details...",
                    "data", Map.of(
                        "reference", reference,
                        "next_step", "card_processing"
                    )
                );
            } else {
                return Map.of(
                    "status", false,
                    "message", "Payment failed. Please try again.",
                    "reference", reference
                );
            }
        } catch (Exception e) {
            logger.error("Error processing callback", e);
            return Map.of(
                "status", false,
                "message", "An error occurred. Please contact support.",
                "reference", reference
            );
        }
    }

    /**
     * ===================================================
     * |                      Webhook                    |
     * ===================================================
     */
    public void processPaystackWebhook(String payload) {
        //logger.info("processPaystackWebhook data = " + payload);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);
            
            String event = webhookData.path("event").asText();
            JsonNode data = webhookData.path("data");

            logger.info("Webhook event: " + event);
            
            if ("charge.success".equals(event) || "authorization.create".equals(event)) {
                String reference = data.path("reference").asText();
                
                // Verify the transaction
                PaystackTransactionVerificationResponse verification = paystackService.verifyTransaction(reference);
                
                if (verification.getStatus() && "success".equals(verification.getData().getStatus())) {
                    // Same logic as your callback method
                    processSuccessfulCardAuthorization(verification);
                }
            } else {
                logger.info("Ignoring webhook event: {}", event);
            }
            
        } catch (Exception e) {
            logger.error("Failed to process webhook payload", e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    private void processSuccessfulCardAuthorization(PaystackTransactionVerificationResponse verification) {
        try {
            // // Check if we already processed this reference
            // String reference = verification.getData().getReference();
            // if (payerDetailsRepository.existsByPaystackReference(reference)) {
            //     logger.warn("Authorization already processed for reference: {}", reference);
            //     return;
            // }
            
            // Extract card authorization details (your existing logic)
            PaystackAuthorization authorization = verification.getData().getAuthorization();
            
            if (authorization == null) {
                logger.error("No authorization data in verification response");
                return;
            }

            // Get user ID from metadata
            UUID userId = UUID.fromString(
                verification.getData().getMetadata().get("user_id").toString());
            
            // Get user details from your database
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Create PayerDetails with captured card information
            CreatePayerDetailsDto createDto = new CreatePayerDetailsDto();
            createDto.setUserId(userId);
            createDto.setType(verification.getData().getMetadata().get("payment_method_type").toString());
            createDto.setEmail(user.getEmail()); // From your user entity, not Paystack
            createDto.setAuthCode(authorization.getAuthorizationCode());
            createDto.setCardType(authorization.getCardType());
            createDto.setLast4(authorization.getLast4());
            createDto.setExpMonth(authorization.getExpMonth());
            createDto.setExpYear(authorization.getExpYear());
            createDto.setBin(authorization.getBin());
            createDto.setBank(authorization.getBank());
            createDto.setSignature(authorization.getSignature());
            
            PayerDetails savedCard = this.addPayerDetails(createDto);
            
            // Remove the pending authorization record
            payerDetailsRepository.findFirstByUserIdAndIsAuthenticatedFalse(userId)
                .ifPresent(pending -> payerDetailsRepository.delete(pending));
            
            logger.info("Card authorization processed successfully for user {} with payer ID {}", 
                    userId, savedCard.getPayerId());
            
            // notificationService.notifyCardSaved(userId, savedCard);
            
        } catch (Exception e) {
            logger.error("Failed to process successful card authorization", e);
            // Retry logic
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String secretKey = paystackKey;
            String computedSignature = computeHmacSha512(payload, secretKey);
        
            return signature.equals(computedSignature);
        } catch (Exception e) {
            logger.error("Failed to verify webhook signature", e);
            return false;
        }
    }

    private String computeHmacSha512(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes());
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

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

    /*
     * Create Paystack transaction request for card capture:
     * - Charges R1.00 in Rands (minimal amount for card auth)
     * - Add metadata to identify this as card authorization
     * - Stores pending authorization details
     */
    public Map<String,Object> initializeCardAuth(InitializeCardAuthDto initDto){
        try {
            String tempRef = generateAuthReference();
            PaystackTransactionRequest request = new PaystackTransactionRequest();
            request.setEmail(initDto.getEmail());
            request.setAmount(100); 
            request.setReference(tempRef);
            request.setCallbackUrl(callbackBaseUrl+"/api/payment-methods/payer/callback");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("purpose", "card_authorization");
            metadata.put("user_id", initDto.getUserId().toString());
            metadata.put("payment_method_type", initDto.getType().toString());
            // metadata.put("temp_reference", tempRef);
            request.setMetadata(metadata);

            PaystackTransactionResponse response = paystackService.initializeTransaction(request);
            
            if (response.getStatus()) {
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

    

    /**
     * ===================================================
     * =                 PayoutDetails                   =
     * ===================================================
     */
    public PayoutDetails addPayoutDetails(CreatePayoutDetailsDto createDto){
        logger.info("Adding payout details for user: {}", createDto.getUserId());

        //Validate user exists
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + createDto.getUserId()));

        validateNoDuplicatePayoutDetails(createDto.getUserId());
        
        PaystackTransferRecipientRequest paystackRecipient = new PaystackTransferRecipientRequest();
        paystackRecipient.setType("basa");
        paystackRecipient.setAccountNumber(createDto.getAccountNumber());
        paystackRecipient.setBankCode(createDto.getBankCode());
        paystackRecipient.setCurrency("ZAR");
        paystackRecipient.setName(createDto.getRecipientName());
        
        PaystackTransferRecipientResponse response;
        try {
            response = paystackService.createTransferRecipient(paystackRecipient);
            if (!response.getStatus()) {
                throw new RuntimeException("Paystack recipient creation failed: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to create Paystack transfer recipient for user {}", createDto.getUserId(), e);
            throw new RuntimeException("Failed to create transfer recipient: " + e.getMessage());
        }

        PayoutDetails pd = new PayoutDetails(createDto.getUserId(), createDto.getType(), createDto.getRecipientName());
        pd.setAccountNumber(createDto.getAccountNumber());
        pd.setBankCode(createDto.getBankCode());
        pd.setBankName(createDto.getBankName());
        pd.setRecipientCode(response.getData().getRecipientCode());
        pd.setIsVerified(true);
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

    // private void deactivateOtherCards(UUID userId, UUID currentPayerId) {
    //     List<PayerDetails> userCards = payerDetailsRepository.findByUserIdAndIsActiveTrue(userId);
    //     userCards.stream()
    //         .filter(card -> !card.getPayerId().equals(currentPayerId))
    //         .forEach(card -> {
    //             card.setIsActive(false);
    //             payerDetailsRepository.save(card);
    //         });
    // }

    private String generateAuthReference() {
        return "AUTH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

}
