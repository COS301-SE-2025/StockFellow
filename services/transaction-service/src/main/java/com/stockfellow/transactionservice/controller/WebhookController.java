package com.stockfellow.transactionservice.controller;
import com.stockfellow.transactionservice.service.PaymentDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Tag(name = "Webhook Controller", description = "Handles event from paystack")
@RequestMapping("/api/transaction/webhook")
@CrossOrigin(origins = "*")
public class WebhookController {
    
    @Autowired
    private PaymentDetailsService paymentDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping
    @Operation(summary = "Handle Paystack webhook", 
            description = "Process webhook notifications from Paystack")
    public ResponseEntity<String> handlePaystackWebhook(@RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {
        
        logger.info("Received Event on Webhook with payload: " + payload);
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
}
