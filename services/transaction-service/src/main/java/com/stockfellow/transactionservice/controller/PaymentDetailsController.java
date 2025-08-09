package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.*;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.*;
import io.swagger.v3.oas.annotations.Operation;

/*TODO:
 * ? - /payer/callback
 * 0 - /payer/webhook
 * 0 - /payer/initialize
 * 0 - /payer/user/{userId} 
 * 0 - /payer/{payerId}/deactivate
 * X - /payout
 * X - /payout/user/{userId}
 */

@RestController
@Tag(name = "Payment Details", description = "Operations related to payment details (card and account info)")
@RequestMapping("/api/payment-methods")
@CrossOrigin(origins = "*")
public class PaymentDetailsController {
    
    @Autowired
    private PaymentDetailsService paymentDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(PaymentDetailsController.class);
    
    // @Autowired
    // private ActivityLogService activityLogService;

    
    /**
     * ===================================================
     * |   Callback (Redirect from Paystack pay page)    |
     * ===================================================
     */
    @GetMapping("/payer/callback")
    @Operation(summary = "Handle Paystack callback", 
               description = "Process callback from Paystack after user completes card authorization")
    public ResponseEntity<Map<String, Object>> handlePaystackCallback(@RequestParam String reference) {
        
        return  ResponseEntity.ok(paymentDetailsService.processPaystackCallback(reference));
    }   

    /**
     * ===================================================
     * |      Webhook (Events from Paystack via ngrok)   |
     * ===================================================
     */
    @PostMapping("/payer/webhook")
    @Operation(summary = "Handle Paystack webhook", 
            description = "Process webhook notifications from Paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {
        
        try {
            if (!paymentDetailsService.verifyWebhookSignature(payload, signature)) {
                logger.warn("Invalid webhook signature");
                return ResponseEntity.status(400).body("Invalid signature");
            }
            
            paymentDetailsService.processPaystackWebhook(payload);
            return ResponseEntity.ok("OK");
            
        } catch (Exception e) {
            logger.error("Failed to process webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }

    /**
     * ===================================================
     * |      PAYER DETAILS (Card/Payment Methods)       |
     * ===================================================
     */
    @PostMapping("/payer/initialize")
    @Operation(summary = "Initialize card authorization", 
               description = "Initialize Paystack payment to capture and save user's card details")
    public ResponseEntity<Map<String, Object>> initializeCardAuthorization(@Valid @RequestBody InitializeCardAuthDto initializeDto) {
        
        return  ResponseEntity.ok(paymentDetailsService.initializeCardAuth(initializeDto));
    }
    
    
    // @PostMapping("/payer")
    // @Operation(summary = "Add a new card", 
    //             description = "Add new card for contributions")
    // public ResponseEntity<PayerDetailsResponseDto> addPayerDetails(@Valid @RequestBody CreatePayerDetailsDto createDto) {
        
    //     PayerDetails created = paymentDetailsService.addPayerDetails(createDto);
    //     return ResponseEntity.status(HttpStatus.CREATED)
    //                        .body(PayerDetailsResponseDto.fromEntity(created));
    // }
    
    @GetMapping("/payer/user/{userId}")
    @Operation(summary = "Get card details", 
                description = "Get card details by user ID")
    public ResponseEntity<List<PayerDetailsResponseDto>> getPayerDetailsByUser(@PathVariable UUID userId) {
        
        List<PayerDetails> payerDetails = paymentDetailsService.findPayerDetailsByUserId(userId);
        return ResponseEntity.ok(payerDetails.stream()
                               .map(PayerDetailsResponseDto::fromEntity)
                               .toList());
    }

    //TODO: How to handle this with paystack?    
    // @PutMapping("/payer/{payerId}")
    // public ResponseEntity<PayerDetailsResponseDto> updatePayerDetails(
    //         @PathVariable UUID payerId,
    //         @Valid @RequestBody UpdatePayerDetailsDto updateDto) {
    //     PayerDetails payerDetails = paymentDetailsService.updatePayerDetails(payerId, updateDto);
    //     return ResponseEntity.ok(PayerDetailsResponseDto.fromEntity(payerDetails));
    // }
    
    // Deactivate card (instead of delete)
    @PutMapping("/payer/{payerId}/deactivate")
    @Operation(summary = "Deactivate card", 
                description = "Sets isActive of PayerDetails to false")
    public ResponseEntity<PayerDetailsResponseDto> deactivateCard(@PathVariable UUID payerId) {
        
        PayerDetails deactivated = paymentDetailsService.deactivateCard(payerId);
        return ResponseEntity.ok(PayerDetailsResponseDto.fromEntity(deactivated));
    }
    
    /**
     * ===================================================
     * |      PAYOUT DETAILS (Bank/Recipient Methods)    |
     * ===================================================
     */
    @PostMapping("/payout")
    @Operation(summary = "Add new payout details (bank account)", 
                description = "Add new payout details (bank account)")
    public ResponseEntity<PayoutDetailsResponseDto> addPayoutDetails(@Valid @RequestBody CreatePayoutDetailsDto createDto) {
        
        PayoutDetails payoutDetails = paymentDetailsService.addPayoutDetails(createDto);
        // activityLogService.logActivity(payoutDetails.getUserId(), null, 
        //                              ActivityLog.EntityType.PAYOUT_DETAILS, payoutDetails.getPayoutId(), 
        //                              "PAYOUT_DETAILS_ADDED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    }
    
    @GetMapping("/payout/user/{userId}")
    @Operation(summary = "Get Payout Details (bank details)", 
                description = "Get Payout details by user ID")
    public ResponseEntity<List<PayoutDetailsResponseDto>> getPayoutDetailsByUser(@PathVariable UUID userId) {
        
        List<PayoutDetails> payoutDetails = paymentDetailsService.findPayoutDetailsByUserId(userId);
        return ResponseEntity.ok(payoutDetails.stream()
                               .map(PayoutDetailsResponseDto::fromEntity)
                               .toList());
    }

    //TODO: How to handle this with paystack?    
    // @PutMapping("/payout/{payoutId}")
    // public ResponseEntity<PayoutDetailsResponseDto> updatePayoutDetails(
    //         @PathVariable UUID payoutId,
    //         @Valid @RequestBody UpdatePayoutDetailsDto updateDto) {
    //     PayoutDetails payoutDetails = paymentDetailsService.updatePayoutDetails(payoutId, updateDto);
    //     return ResponseEntity.ok(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    // }
    
    //TODO: not needed. A user should only have 1 payout method
    // @PutMapping("/payout/{payoutId}/set-default")
    // public ResponseEntity<PayoutDetailsResponseDto> setActivePayoutMethod(@PathVariable UUID payoutId) {
    //     PayoutDetails payoutDetails = paymentDetailsService.setAsActive(payoutId);
    //     return ResponseEntity.ok(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    // }
    
    // @DeleteMapping("/payout/{payoutId}")
    // public ResponseEntity<Void> deletePayoutDetails(@PathVariable UUID payoutId) {
    //     paymentDetailsService.deletePayoutDetails(payoutId);
    //     return ResponseEntity.noContent().build();
    // }
}
