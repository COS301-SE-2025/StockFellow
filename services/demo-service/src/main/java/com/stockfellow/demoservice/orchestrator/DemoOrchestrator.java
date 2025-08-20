// DemoOrchestrator.java - Thorough Debug Version
package com.stockfellow.demoservice.orchestrator;

import com.stockfellow.demoservice.dto.DemoRequest;
import com.stockfellow.demoservice.dto.RegistrationResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@Service
public class DemoOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(DemoOrchestrator.class);
    
    @Autowired
    private UserRegistrationService userRegistrationService;
    
    private final RestTemplate restTemplate;
    private String baseUrl = "http://10.0.2.2:3000/api";
    
    // Result class to return detailed information
    public static class DemoResult {
        private boolean success;
        private String message;
        private Map<String, Object> details;
        
        public DemoResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.details = new HashMap<>();
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Map<String, Object> getDetails() { return details; }
        public void addDetail(String key, Object value) { this.details.put(key, value); }
    }
    
    // Constructor
    public DemoOrchestrator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        logger.info("=== DEMO ORCHESTRATOR INITIALIZED ===");
        logger.info("RestTemplate instance: {}", restTemplate != null ? "PRESENT" : "NULL");
        logger.info("Base URL configured: {}", baseUrl);
    }
    
    public DemoResult startDemo(DemoRequest session) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("=== DEMO ORCHESTRATOR START ===");
            logger.info("Session details: scenarioType='{}', sessionId='{}'", 
                session.getScenarioType(), session.getSessionId());
            
            // Validate scenario type
            if (session.getScenarioType() == null) {
                logger.error("Scenario type is null");
                return new DemoResult(false, "Scenario type cannot be null");
            }
            
            String scenarioType = session.getScenarioType().trim();
            logger.info("Processing scenario type: '{}'", scenarioType);
            
            if (!scenarioType.equals("Group Cycles")) {
                logger.warn("Unsupported scenario type: '{}'. Only 'Group Cycles' is supported.", scenarioType);
                DemoResult result = new DemoResult(false, "Currently only support: Group Cycles");
                result.addDetail("supportedScenarios", Arrays.asList("Group Cycles"));
                result.addDetail("requestedScenario", scenarioType);
                return result;
            }
            
            logger.info("Scenario type validated successfully: '{}'", scenarioType);
            session.setStatus("VALIDATED");
            
            // Execute demo
            DemoResult result = executeDemo(session);
            
            long executionTime = System.currentTimeMillis() - startTime;
            result.addDetail("totalExecutionTimeMs", executionTime);
            
            logger.info("=== DEMO ORCHESTRATOR COMPLETE === Success: {}, Time: {}ms", 
                result.isSuccess(), executionTime);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("=== DEMO ORCHESTRATOR EXCEPTION ===", e);
            logger.error("Exception after {}ms: {} - {}", executionTime, e.getClass().getSimpleName(), e.getMessage());
            
            session.setStatus("FAILED");
            session.setMessage(e.getMessage());
            
            DemoResult result = new DemoResult(false, "Demo execution failed: " + e.getMessage());
            result.addDetail("exceptionType", e.getClass().getSimpleName());
            result.addDetail("executionTimeMs", executionTime);
            
            return result;
        }
    }
    
    private DemoResult executeDemo(DemoRequest session) {
        long startTime = System.currentTimeMillis();
        DemoResult result = new DemoResult(true, "Demo executed successfully");
        
        try {
            logger.info("--- STEP 1: USER REGISTRATION START ---");
            session.setStatus("REGISTERING_USERS");
            updateStatus(session, "Registering 5 demo users via gateway...");
            
            // Check if UserRegistrationService is available
            if (userRegistrationService == null) {
                logger.error("UserRegistrationService is NULL - dependency injection failed!");
                return new DemoResult(false, "UserRegistrationService not available");
            }
            
            logger.info("UserRegistrationService is available, calling registerUsers()...");
            
            long registrationStartTime = System.currentTimeMillis();
            UserRegistrationService.RegistrationResult registrationResult = userRegistrationService.registerUsers();
            long registrationTime = System.currentTimeMillis() - registrationStartTime;
            
            logger.info("registerUsers() completed in {}ms", registrationTime);
            logger.info("Registration result: success={}, successCount={}, totalCount={}", 
                registrationResult.isOverallSuccess(), 
                registrationResult.getSuccessfulCount(),
                registrationResult.getTotalCount());
            
            result.addDetail("registrationTimeMs", registrationTime);
            result.addDetail("usersRegistered", registrationResult.getSuccessfulCount());
            result.addDetail("totalRegistrationAttempts", registrationResult.getTotalCount());
            result.addDetail("registrationErrors", registrationResult.getErrors());
            
            if (!registrationResult.isOverallSuccess()) {
                logger.error("User registration failed completely");
                session.setStatus("REGISTRATION_FAILED");
                return new DemoResult(false, "Failed to register users: " + registrationResult.getErrorSummary());
            }
            
            if (registrationResult.getSuccessfulCount() == 0) {
                logger.error("No users were successfully registered");
                session.setStatus("REGISTRATION_FAILED");
                return new DemoResult(false, "No users were successfully registered");
            }
            
            logger.info("--- STEP 1: USER REGISTRATION SUCCESS ---");
            logger.info("Successfully registered {} out of {} users", 
                registrationResult.getSuccessfulCount(), registrationResult.getTotalCount());
            
            // Continue with rest of demo...
            logger.info("--- STEP 2: PAYMENT DETAILS START ---");
            session.setStatus("REGISTERING_PAYMENT_DETAILS");
            updateStatus(session, "Registering payment details...");
            
            // Add more demo steps here as needed
            Thread.sleep(1000); // Simulate some work
            
            logger.info("--- STEP 2: PAYMENT DETAILS SUCCESS ---");
            
            session.setStatus("COMPLETED");
            result.setMessage("Demo completed successfully with " + registrationResult.getSuccessfulCount() + " users registered");
            
            long totalTime = System.currentTimeMillis() - startTime;
            result.addDetail("demoExecutionTimeMs", totalTime);
            
            logger.info("=== DEMO EXECUTION COMPLETE === Total time: {}ms", totalTime);
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("=== DEMO EXECUTION FAILED ===", e);
            logger.error("Failed after {}ms: {}", executionTime, e.getMessage());
            
            session.setStatus("FAILED");
            session.setMessage("Demo execution failed: " + e.getMessage());
            
            result = new DemoResult(false, "Demo execution failed: " + e.getMessage());
            result.addDetail("executionTimeMs", executionTime);
            result.addDetail("exceptionType", e.getClass().getSimpleName());
            
            return result;
        }
    }
    
    public void updateStatus(DemoRequest session, String status) {
        logger.info("STATUS UPDATE: sessionId='{}', status='{}'", session.getSessionId(), status);
        session.setStatus(status);
    }
    
    public void cleanup(String sessionId) {
        logger.info("=== CLEANUP START === for session: {}", sessionId);
        try {
            // Add actual cleanup logic here
            logger.info("Cleanup completed for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Cleanup failed for session: {}", sessionId, e);
            throw e;
        }
    }
}