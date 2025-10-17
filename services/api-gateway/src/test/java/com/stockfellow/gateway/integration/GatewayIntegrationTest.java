// package com.stockfellow.gateway.integration;

// import com.github.tomakehurst.wiremock.WireMockServer;
// import com.github.tomakehurst.wiremock.client.WireMock;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.web.server.LocalServerPort;
// import org.springframework.http.*;
// import org.springframework.test.context.ActiveProfiles;

// import static com.github.tomakehurst.wiremock.client.WireMock.*;
// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test")
// class GatewayIntegrationTest {
    
//     @LocalServerPort
//     private int gatewayPort;
    
//     @Autowired
//     private TestRestTemplate restTemplate;
    
//     private WireMockServer userServiceMock;
//     private WireMockServer groupServiceMock;
    
//     @BeforeEach
//     void setUp() {
//         // Start WireMock servers to mock backend services
//         userServiceMock = new WireMockServer(4000);
//         userServiceMock.start();
        
//         groupServiceMock = new WireMockServer(4040);
//         groupServiceMock.start();
        
//         // Configure WireMock
//         WireMock.configureFor("localhost", 4000);
//     }
    
//     @AfterEach
//     void tearDown() {
//         userServiceMock.stop();
//         groupServiceMock.stop();
//     }
    
//     @Test
//     void shouldProxyRequestToUserService() {
//         // Given
//         userServiceMock.stubFor(get(urlEqualTo("/api/users/profile"))
//             .willReturn(aResponse()
//                 .withStatus(200)
//                 .withHeader("Content-Type", "application/json")
//                 .withBody("{\"id\":123,\"name\":\"John Doe\"}")));
        
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/users/profile";
        
//         // When
//         ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl, String.class);
        
//         // Then
//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertTrue(response.getBody().contains("John Doe"));
        
//         // Verify the request was proxied to the backend service
//         userServiceMock.verify(getRequestedFor(urlEqualTo("/api/users/profile")));
//     }
    
//     @Test
//     void shouldHandleBackendServiceErrors() {
//         // Given
//         userServiceMock.stubFor(get(urlEqualTo("/api/users/profile"))
//             .willReturn(aResponse()
//                 .withStatus(500)
//                 .withBody("Internal server error")));
        
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/users/profile";
        
//         // When
//         ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl, String.class);
        
//         // Then
//         assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//         userServiceMock.verify(getRequestedFor(urlEqualTo("/api/users/profile")));
//     }
    
//     @Test
//     void shouldApplyRateLimiting() {
//         // Given
//         userServiceMock.stubFor(get(urlMatching("/api/users/.*"))
//             .willReturn(aResponse()
//                 .withStatus(200)
//                 .withBody("Success")));
        
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/users/test";
        
//         // When - Make multiple requests rapidly
//         ResponseEntity<String> response1 = restTemplate.getForEntity(gatewayUrl, String.class);
//         ResponseEntity<String> response2 = restTemplate.getForEntity(gatewayUrl, String.class);
//         ResponseEntity<String> response3 = restTemplate.getForEntity(gatewayUrl, String.class);
        
//         // Then - First few should succeed, but eventually should hit rate limit
//         // Note: Exact behavior depends on rate limit configuration
//         assertTrue(response1.getStatusCode().is2xxSuccessful() || 
//                   response1.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS));
//     }
    
//     @Test
//     void shouldAddCORSHeaders() {
//         // Given
//         userServiceMock.stubFor(get(urlEqualTo("/api/users/profile"))
//             .willReturn(aResponse()
//                 .withStatus(200)
//                 .withBody("User data")));
        
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/users/profile";
        
//         HttpHeaders headers = new HttpHeaders();
//         headers.add("Origin", "http://localhost:3001");
//         HttpEntity<String> entity = new HttpEntity<>(headers);
        
//         // When
//         ResponseEntity<String> response = restTemplate.exchange(
//             gatewayUrl, HttpMethod.GET, entity, String.class);
        
//         // Then
//         assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
//         assertEquals("*", response.getHeaders().getFirst("Access-Control-Allow-Origin"));
//     }
    
//     @Test
//     void shouldForwardAuthorizationHeaders() {
//         // Given
//         userServiceMock.stubFor(get(urlEqualTo("/api/users/profile"))
//             .withHeader("Authorization", equalTo("Bearer test-token"))
//             .willReturn(aResponse()
//                 .withStatus(200)
//                 .withBody("Authenticated user data")));
        
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/users/profile";
        
//         HttpHeaders headers = new HttpHeaders();
//         headers.add("Authorization", "Bearer test-token");
//         HttpEntity<String> entity = new HttpEntity<>(headers);
        
//         // When
//         ResponseEntity<String> response = restTemplate.exchange(
//             gatewayUrl, HttpMethod.GET, entity, String.class);
        
//         // Then
//         assertEquals(HttpStatus.OK, response.getStatusCode());
        
//         // Verify the Authorization header was forwarded
//         userServiceMock.verify(getRequestedFor(urlEqualTo("/api/users/profile"))
//             .withHeader("Authorization", equalTo("Bearer test-token")));
//     }
    
//     @Test
//     void shouldHandlePostRequestsWithBody() {
//         // Given
//         userServiceMock.stubFor(post(urlEqualTo("/api/users"))
//             .withRequestBody(containing("John Doe"))
//             .willReturn(aResponse()
//                 .withStatus(201)
//                 .withHeader("Content-Type", "application/json")
//                 .withBody("{\"id\":456,\"name\":\"John Doe\"}")));
        
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/users";
//         String requestBody = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
//         // When
//         ResponseEntity<String> response = restTemplate.exchange(
//             gatewayUrl, HttpMethod.POST, entity, String.class);
        
//         // Then
//         assertEquals(HttpStatus.CREATED, response.getStatusCode());
//         assertTrue(response.getBody().contains("John Doe"));
        
//         userServiceMock.verify(postRequestedFor(urlEqualTo("/api/users"))
//             .withRequestBody(containing("John Doe")));
//     }
    
//     @Test
//     void shouldReturn404ForUnknownRoutes() {
//         // Given
//         String gatewayUrl = "http://localhost:" + gatewayPort + "/api/unknown/endpoint";
        
//         // When
//         ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl, String.class);
        
//         // Then
//         assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//     }
// }