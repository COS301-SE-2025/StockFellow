package com.stockfellow.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${services.user-service.url:http://user-service:4020}")
    private String userServiceUrl;
    
    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Creates a user record in the user service after successful Keycloak registration
     */
    public void createUserRecord(String userId, RegisterRequest registerRequest) {
        try {
            String url = userServiceUrl + "/api/users/create";
            
            // Create user creation request
            Map<String, Object> userCreateRequest = Map.of(
                "userId", userId,
                "username", registerRequest.getUsername(),
                "email", registerRequest.getEmail(),
                "firstName", registerRequest.getFirstName() != null ? registerRequest.getFirstName() : "",
                "lastName", registerRequest.getLastName() != null ? registerRequest.getLastName() : "",
                "verified", false,
                "createdAt", System.currentTimeMillis()
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userCreateRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("User record created successfully in user service for userId: {}", userId);
            } else {
                logger.warn("Failed to create user record in user service. Status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error creating user record in user service for userId: {}", userId, e);
            throw new RuntimeException("Failed to create user record", e);
        }
    }

    /**
     * Create user in User Service database after Keycloak registration
     * forward to user service to create user record
     */
   public Map<String, Object> createUser(String userId, String username, String email, String firstName, String lastName) {
    
        try {
            String url = userServiceUrl + "/api/users/register";
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("username", username);
            requestBody.put("email", email);
            requestBody.put("firstName", firstName);
            requestBody.put("lastName", lastName);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Gateway-Request", "true"); // Identify as gateway request
            headers.set("X-Service-Auth", "gateway-service"); // Add service authentication
            // Alternative: Use a shared secret or service token
            // headers.set("X-Service-Token", serviceToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            logger.info("Creating user in User Service: userId={}, username={}", userId, username);

            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("User created successfully in User Service: {}", username);
                return (Map<String, Object>) response.getBody();
            } else {
                logger.error("User Service returned non-success status: {} for user: {}", 
                        response.getStatusCode(), username);
                return Map.of("error", "User creation failed", "status", response.getStatusCode().value());
            }

        } catch (HttpClientErrorException e) {
            logger.error("Client error creating user in User Service: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of("error", "User creation failed", "message", e.getResponseBodyAsString(), "status", e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            logger.error("Server error creating user in User Service: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of("error", "User service error", "message", "Internal server error in user service", "status", e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            logger.error("Connection error to User Service", e);
            return Map.of("error", "Service unavailable", "message", "Could not connect to user service");
        } catch (Exception e) {
            logger.error("Unexpected error creating user in User Service", e);
            return Map.of("error", "Unexpected error", "message", e.getMessage());
        }
    }

    
    /**
     * Forwards the ID verification request to the user service
     */
    public Map<String, Object> verifyUserID(MultipartFile file, String userId, String authHeader) {
        try {
            String url = userServiceUrl + "/api/users/verifyID";
            
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", authHeader);
            headers.set("X-User-Id", userId);
            
            // Create multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add file as ByteArrayResource to preserve filename and content type
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            body.add("file", fileResource);
            body.add("userId", userId);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            logger.info("Forwarding ID verification request to user service for userId: {}", userId);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("ID verification request processed successfully by user service for userId: {}", userId);
                return response.getBody();
            } else {
                logger.warn("User service returned error status: {} for userId: {}", 
                           response.getStatusCode(), userId);
                return Map.of(
                    "error", "verification_failed",
                    "message", "User service returned error: " + response.getStatusCode()
                );
            }
            
        } catch (IOException e) {
            logger.error("Error reading file content for ID verification, userId: {}", userId, e);
            return Map.of(
                "error", "file_processing_error",
                "message", "Error processing the uploaded file"
            );
        } catch (Exception e) {
            logger.error("Error forwarding ID verification request to user service for userId: {}", userId, e);
            return Map.of(
                "error", "service_communication_error",
                "message", "Failed to communicate with user service"
            );
        }
    }
    
    /**
     * Gets user profile from user service
     */
    public Map<String, Object> getUserProfile(String userId, String authHeader) {
        try {
            String url = userServiceUrl + "/api/users/profile";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("X-User-Id", userId);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return Map.of(
                    "error", "profile_fetch_failed",
                    "message", "Failed to fetch user profile"
                );
            }
            
        } catch (Exception e) {
            logger.error("Error fetching user profile from user service for userId: {}", userId, e);
            return Map.of(
                "error", "service_communication_error",
                "message", "Failed to communicate with user service"
            );
        }
    }
    
    /**
     * Gets user by ID from user service
     */
    public Map<String, Object> getUserById(String userId, String requestingUserId, String authHeader) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("X-User-Id", requestingUserId);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return Map.of(
                    "error", "user_fetch_failed",
                    "message", "Failed to fetch user details"
                );
            }
            
        } catch (Exception e) {
            logger.error("Error fetching user by ID from user service. UserId: {}, RequestingUserId: {}", 
                        userId, requestingUserId, e);
            return Map.of(
                "error", "service_communication_error",
                "message", "Failed to communicate with user service"
            );
        }
    }
    
    // Inner class for register request (can be moved to separate file if needed)
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}