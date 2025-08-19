// DemoController.java - Thorough Debug Version
package com.stockfellow.demoservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.stockfellow.demoservice.dto.DemoRequest;
import com.stockfellow.demoservice.orchestrator.DemoOrchestrator;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    
    @Autowired
    private DemoOrchestrator demoOrchestrator;
    
    // Request body class for proper JSON deserialization
    public static class StartDemoRequest {
        private String scenarioType;
        
        public String getScenarioType() { return scenarioType; }
        public void setScenarioType(String scenarioType) { this.scenarioType = scenarioType; }
    }
    
    // API endpoint for programmatic access
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startDemo(@RequestBody StartDemoRequest request) {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("=== DEMO CONTROLLER START ===");
            logger.info("Received request: {}", request);
            logger.info("Request scenarioType: '{}'", request.getScenarioType());
            
            // Validate input
            if (request == null || request.getScenarioType() == null || request.getScenarioType().trim().isEmpty()) {
                logger.error("Invalid scenario type: null or empty");
                response.put("success", false);
                response.put("error", "Scenario type cannot be null or empty");
                response.put("scenarioType", request != null ? request.getScenarioType() : null);
                return ResponseEntity.badRequest().body(response);
            }
            
            String cleanScenarioType = request.getScenarioType().trim();
            logger.info("Clean scenario type: '{}'", cleanScenarioType);
            
            DemoRequest demoRequest = new DemoRequest();
            demoRequest.setScenarioType(cleanScenarioType);
            demoRequest.setSessionId("demo-session-1");
            demoRequest.setStatus("STARTING");
            
            logger.info("Created DemoRequest object: scenarioType='{}', sessionId='{}'", 
                demoRequest.getScenarioType(), demoRequest.getSessionId());
            
            // Call orchestrator
            logger.info("Calling DemoOrchestrator.startDemo()...");
            DemoOrchestrator.DemoResult result = demoOrchestrator.startDemo(demoRequest);
            
            logger.info("DemoOrchestrator returned: success={}, message='{}'", 
                result.isSuccess(), result.getMessage());
            
            // Build response
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("scenarioType", cleanScenarioType);
            response.put("sessionId", demoRequest.getSessionId());
            response.put("status", demoRequest.getStatus());
            response.put("executionTimeMs", System.currentTimeMillis() - startTime);
            
            if (result.isSuccess()) {
                logger.info("Demo completed successfully in {}ms", System.currentTimeMillis() - startTime);
                return ResponseEntity.ok(response);
            } else {
                logger.error("Demo failed: {}", result.getMessage());
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("=== DEMO CONTROLLER EXCEPTION ===", e);
            logger.error("Exception type: {}", e.getClass().getSimpleName());
            logger.error("Exception message: {}", e.getMessage());
            logger.error("Execution time before error: {}ms", executionTime);
            
            response.put("success", false);
            response.put("error", "Demo failed with exception: " + e.getMessage());
            response.put("exceptionType", e.getClass().getSimpleName());
            response.put("scenarioType", request != null ? request.getScenarioType() : null);
            response.put("sessionId", "demo-session-1");
            response.put("executionTimeMs", executionTime);
            
            return ResponseEntity.status(500).body(response);
        } finally {
            logger.info("=== DEMO CONTROLLER END === (Total time: {}ms)", 
                System.currentTimeMillis() - startTime);
        }
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "DemoController");
        health.put("timestamp", System.currentTimeMillis());
        
        logger.info("Health check called - service is UP");
        return ResponseEntity.ok(health);
    }
    
    // Cleanup demo data
    @DeleteMapping("/cleanup/{sessionId}")
    public ResponseEntity<Map<String, Object>> cleanupDemo(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("=== CLEANUP START === for session: {}", sessionId);
            demoOrchestrator.cleanup(sessionId);
            
            response.put("success", true);
            response.put("message", "Demo data cleaned up successfully");
            response.put("sessionId", sessionId);
            
            logger.info("Cleanup completed successfully for session: {}", sessionId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== CLEANUP FAILED === for session: {}", sessionId, e);
            
            response.put("success", false);
            response.put("error", "Cleanup failed: " + e.getMessage());
            response.put("sessionId", sessionId);
            
            return ResponseEntity.status(500).body(response);
        }
    }
}