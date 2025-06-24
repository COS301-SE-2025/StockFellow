// package com.stockfellow.gateway.integration;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.web.server.LocalServerPort;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.ActiveProfiles;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test")
// class DebugIntegrationTest {
    
//     @LocalServerPort
//     private int port;
    
//     @Autowired
//     private TestRestTemplate restTemplate;
    
//     @Test
//     void debugApiEndpointResponse() {
//         // Test what happens when we hit an API endpoint
//         ResponseEntity<String> response = restTemplate.getForEntity(
//             "http://localhost:" + port + "/api/users", String.class);
        
//         System.out.println("=== DEBUG API ENDPOINT ===");
//         System.out.println("Status: " + response.getStatusCode());
//         System.out.println("Headers: " + response.getHeaders());
//         System.out.println("Body: " + response.getBody());
//         System.out.println("========================");
        
//         // Basic assertion - should get some response
//         assertNotNull(response);
//         assertNotNull(response.getStatusCode());
//     }
    
//     @Test
//     void debugHealthEndpoint() {
//         // Test health endpoint for comparison
//         ResponseEntity<String> response = restTemplate.getForEntity(
//             "http://localhost:" + port + "/actuator/health", String.class);
        
//         System.out.println("=== DEBUG HEALTH ENDPOINT ===");
//         System.out.println("Status: " + response.getStatusCode());
//         System.out.println("Body: " + response.getBody());
//         System.out.println("=============================");
        
//         // Health should work
//         assertTrue(response.getStatusCode().is2xxSuccessful());
//     }
    
//     @Test
//     void debugErrorEndpoint() {
//         // Test what a normal 404 looks like
//         ResponseEntity<String> response = restTemplate.getForEntity(
//             "http://localhost:" + port + "/nonexistent", String.class);
        
//         System.out.println("=== DEBUG 404 ENDPOINT ===");
//         System.out.println("Status: " + response.getStatusCode());
//         System.out.println("Body: " + response.getBody());
//         System.out.println("=========================");
        
//         // Should be 404, not 500
//         assertEquals(404, response.getStatusCode().value());
//     }
// }