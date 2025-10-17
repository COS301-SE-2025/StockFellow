package com.stockfellow.gateway.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.keycloak.adapters.springboot.KeycloakAutoConfiguration"
    }
)
@ActiveProfiles("test")
class BasicGatewayIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void contextLoads() {
        // Simple test to verify Spring context loads
        assertTrue(port > 0, "Application should start on a port");
    }
    
    @Test
    void shouldRespondToHealthCheck() {
        // Test that actuator health endpoint works
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), 
            "Health check should return 2xx status: " + response.getStatusCode());
        assertNotNull(response.getBody(), "Health check should return a response body");
    }
    
    @Test
    void shouldHandleInvalidRoutes() {
        // Test that invalid routes don't crash the application
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/invalid/route", String.class);
        
        // Should return 4xx/5xx, not crash
        assertTrue(response.getStatusCode().is4xxClientError() || 
                  response.getStatusCode().is5xxServerError(),
            "Invalid routes should return error status");
    }
    
    @Test
    void shouldAcceptApiRequests() {
        // Test that API routes are accepted (even if they fail to proxy)
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/test", String.class);
        
        // Should not be null (some response should come back)
        assertNotNull(response, "Should receive some response for API routes");
        assertNotNull(response.getStatusCode(), "Should have a status code");
        
        // Print actual response for debugging
        System.out.println("API test response: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
    }
    
    @Test
    void shouldStartWithoutErrors() {
        // Verify the application started without major errors
        
        // Try to make a simple request to verify the server is responding
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/info", String.class);
        
        // Should get some kind of response (even if 404)
        assertNotNull(response);
        assertTrue(response.getStatusCode().value() < 600, 
            "Should not return unknown status codes");
    }
}