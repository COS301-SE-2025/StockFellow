// UserRegistrationService.java - Thorough Debug Version
package com.stockfellow.demoservice.orchestrator;

import com.stockfellow.demoservice.dto.RegisterRequest;
import com.stockfellow.demoservice.dto.RegistrationResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.*;

@Service
public class UserRegistrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    // Make this configurable via application.properties
    @Value("${app.auth-service.base-url:http://host.docker.internal:3000/api}")
    private String baseUrl;
    
    // Result class to return detailed registration information
    public static class RegistrationResult {
        private List<RegistrationResponse> responses;
        private List<String> errors;
        private int successfulCount;
        private int totalCount;
        private boolean overallSuccess;
        private Map<String, Object> details;
        
        public RegistrationResult() {
            this.responses = new ArrayList<>();
            this.errors = new ArrayList<>();
            this.details = new HashMap<>();
        }
        
        // Getters and setters
        public List<RegistrationResponse> getResponses() { return responses; }
        public List<String> getErrors() { return errors; }
        public int getSuccessfulCount() { return successfulCount; }
        public void setSuccessfulCount(int successfulCount) { this.successfulCount = successfulCount; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public boolean isOverallSuccess() { return overallSuccess; }
        public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
        public Map<String, Object> getDetails() { return details; }
        public void addDetail(String key, Object value) { this.details.put(key, value); }
        
        public String getErrorSummary() {
            if (errors.isEmpty()) return "No errors";
            return String.join("; ", errors);
        }
    }
    
    public RegistrationResult registerUsers() {
        long startTime = System.currentTimeMillis();
        RegistrationResult result = new RegistrationResult();
        
        try {
            logger.info("=== USER REGISTRATION SERVICE START ===");
            logger.info("Using auth service base URL: '{}'", baseUrl);
            logger.info("RestTemplate instance: {}", restTemplate != null ? "PRESENT" : "NULL");
            
            if (restTemplate == null) {
                logger.error("RestTemplate is NULL - cannot proceed with registration");
                result.getErrors().add("RestTemplate not available");
                result.setOverallSuccess(false);
                return result;
            }
            
            // Test connectivity first
            if (!testConnectivity()) {
                logger.error("Connectivity test failed - cannot reach auth service");
                result.getErrors().add("Cannot reach auth service at: " + baseUrl);
                result.setOverallSuccess(false);
                return result;
            }
            
            String[] usernames = {
                "testuser1", "testuser2", "testuser3", "testuser4", "testuser5"
            };
            String[] firstNames = {
                "User1", "User2", "User3", "User4", "User5"
            };
            String[] lastNames = {
                "One", "Two", "Three", "Four", "Five"
            };
            String[] userEmails = {
                "testuser1@mail.com", "testuser2@mail.com", "testuser3@mail.com", 
                "testuser4@mail.com", "testuser5@mail.com"
            };
            String userPassword = "pass123!";
            String userPhone = "0829458879";
            String userId = "0302045129087";
            
            result.setTotalCount(usernames.length);
            logger.info("Attempting to register {} users", usernames.length);
            
            int successCount = 0;
            
            for (int i = 0; i < usernames.length; i++) {
                long userStartTime = System.currentTimeMillis();
                String currentUser = usernames[i];
                
                try {
                    logger.info("--- REGISTERING USER {} of {} ---", i + 1, usernames.length);
                    logger.info("Username: '{}', Email: '{}'", currentUser, userEmails[i]);
                    
                    RegisterRequest request = new RegisterRequest(
                        usernames[i],
                        firstNames[i],
                        lastNames[i],
                        userEmails[i],
                        userPassword,
                        userPhone + i, // Make phone numbers unique
                        userId + i     // Make ID numbers unique
                    );
                    
                    logger.info("Created RegisterRequest: username='{}', firstName='{}', lastName='{}', email='{}'", 
                        request.getUsername(), request.getFirstName(), request.getLastName(), request.getEmail());
                    
                    HttpHeaders headers = createHeadersWithoutAuth();
                    HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);
                    
                    String registrationUrl = baseUrl + "/auth/register";
                    logger.info("Making POST request to: {}", registrationUrl);
                    logger.info("Request headers: {}", headers);
                    logger.info("Request body: username='{}', email='{}'", request.getUsername(), request.getEmail());
                    
                    long requestStartTime = System.currentTimeMillis();
                    ResponseEntity<RegistrationResponse> response = restTemplate.exchange(
                        registrationUrl,
                        HttpMethod.POST,
                        entity,
                        RegistrationResponse.class
                    );
                    long requestTime = System.currentTimeMillis() - requestStartTime;
                    
                    logger.info("Received response in {}ms: status={}", requestTime, response.getStatusCode());
                    logger.info("Response body: {}", response.getBody());
                    
                    RegistrationResponse registrationResponse = response.getBody();
                    if (registrationResponse != null) {
                        result.getResponses().add(registrationResponse);
                        successCount++;
                        
                        logger.info("✓ Successfully registered user: '{}' ({}ms)", currentUser, 
                            System.currentTimeMillis() - userStartTime);
                    } else {
                        logger.warn("Registration response body is null for user: {}", currentUser);
                        result.getErrors().add("Null response for user: " + currentUser);
                    }
                    
                } catch (HttpClientErrorException e) {
                    long userTime = System.currentTimeMillis() - userStartTime;
                    logger.warn("✗ HTTP Client Error for user '{}' after {}ms:", currentUser, userTime);
                    logger.warn("  Status: {}", e.getStatusCode());
                    logger.warn("  Response: {}", e.getResponseBodyAsString());
                    logger.warn("  Headers: {}", e.getResponseHeaders());
                    
                    String errorMsg = String.format("HTTP %s for user %s: %s", 
                        e.getStatusCode(), currentUser, e.getResponseBodyAsString());
                    result.getErrors().add(errorMsg);
                    
                    RegistrationResponse errorResponse = new RegistrationResponse();
                    errorResponse.setError(errorMsg);
                    result.getResponses().add(errorResponse);
                    
                } catch (ResourceAccessException e) {
                    long userTime = System.currentTimeMillis() - userStartTime;
                    logger.error("✗ Connection Error for user '{}' after {}ms:", currentUser, userTime);
                    logger.error("  Error type: {}", e.getClass().getSimpleName());
                    logger.error("  Message: {}", e.getMessage());
                    
                    if (e.getCause() instanceof ConnectException) {
                        logger.error("  Root cause: Connection refused - service may be down");
                    } else if (e.getCause() instanceof SocketTimeoutException) {
                        logger.error("  Root cause: Socket timeout - service may be slow");
                    }
                    
                    String errorMsg = String.format("Connection failed for user %s: %s", currentUser, e.getMessage());
                    result.getErrors().add(errorMsg);
                    
                    RegistrationResponse errorResponse = new RegistrationResponse();
                    errorResponse.setError(errorMsg);
                    result.getResponses().add(errorResponse);
                    
                } catch (Exception e) {
                    long userTime = System.currentTimeMillis() - userStartTime;
                    logger.error("✗ Unexpected Error for user '{}' after {}ms:", currentUser, userTime, e);
                    logger.error("  Exception type: {}", e.getClass().getSimpleName());
                    logger.error("  Message: {}", e.getMessage());
                    
                    String errorMsg = String.format("Unexpected error for user %s: %s", currentUser, e.getMessage());
                    result.getErrors().add(errorMsg);
                    
                    RegistrationResponse errorResponse = new RegistrationResponse();
                    errorResponse.setError(errorMsg);
                    result.getResponses().add(errorResponse);
                }
            }
            
            result.setSuccessfulCount(successCount);
            result.setOverallSuccess(successCount > 0);
            
            long totalTime = System.currentTimeMillis() - startTime;
            result.addDetail("totalExecutionTimeMs", totalTime);
            result.addDetail("averageTimePerUser", totalTime / usernames.length);
            
            logger.info("=== USER REGISTRATION COMPLETE ===");
            logger.info("Total time: {}ms", totalTime);
            logger.info("Success rate: {}/{} ({}%)", successCount, usernames.length, 
                (successCount * 100) / usernames.length);
            logger.info("Overall success: {}", result.isOverallSuccess());
            
            if (!result.getErrors().isEmpty()) {
                logger.info("Errors encountered:");
                for (String error : result.getErrors()) {
                    logger.info("  - {}", error);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("=== USER REGISTRATION FAILED ===", e);
            logger.error("Failed after {}ms with exception: {}", totalTime, e.getMessage());
            
            result.getErrors().add("Service failed: " + e.getMessage());
            result.setOverallSuccess(false);
            result.addDetail("totalExecutionTimeMs", totalTime);
            result.addDetail("exceptionType", e.getClass().getSimpleName());
            
            return result;
        }
    }
    
    private boolean testConnectivity() {
        try {
            logger.info("Testing connectivity to auth service...");
            
            // Try a simple GET to a health endpoint or the base URL
            String testUrl = baseUrl + "/health";
            logger.info("Testing connectivity with URL: {}", testUrl);
            
            HttpHeaders headers = createHeadersWithoutAuth();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                testUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            long responseTime = System.currentTimeMillis() - startTime;
            
            logger.info("Connectivity test successful: status={}, time={}ms", 
                response.getStatusCode(), responseTime);
            return true;
            
        } catch (Exception e) {
            logger.warn("Connectivity test failed (this may be normal if health endpoint doesn't exist): {}", e.getMessage());
            // Don't fail completely if health check fails - the registration endpoint might still work
            return true;
        }
    }
    
    private HttpHeaders createHeadersWithoutAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "DemoService/1.0");
        
        logger.debug("Created headers: Content-Type={}, Accept={}", 
            headers.getContentType(), headers.getAccept());
        
        return headers;
    }
}