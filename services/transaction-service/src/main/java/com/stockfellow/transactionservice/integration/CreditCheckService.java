package com.stockfellow.transactionservice.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreditCheckService {
    private static final Logger logger = LoggerFactory.getLogger(CreditCheckService.class);

    public String performCreditCheck(String userId, String idNumber) {
        // Mock implementation (replace with real API call to TransUnion/Experian)
        logger.info("Performing credit check for user: {}", userId);
        try {
            // Simulate API call
            Thread.sleep(1000); // Simulate network delay
            // Return tier based on mock logic
            return idNumber.endsWith("0") ? "LOW_RISK" : "MEDIUM_RISK";
        } catch (InterruptedException e) {
            logger.error("Credit check failed: {}", e.getMessage());
            throw new RuntimeException("Credit check failed", e);
        }
    }
}
