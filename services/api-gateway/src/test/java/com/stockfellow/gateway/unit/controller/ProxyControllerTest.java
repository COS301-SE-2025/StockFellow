package com.stockfellow.gateway.unit.controller;

import com.stockfellow.gateway.controller.ProxyController;
import com.stockfellow.gateway.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProxyControllerTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private HttpServletRequest request;
    
    private ProxyController proxyController;
    private List<Route> testRoutes;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test routes
        testRoutes = Arrays.asList(
            new Route("/api/users", false, 
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy("http://user-service:4000", true)),
            new Route("/api/user", false,
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy("http://user-service:4000", true)),
            new Route("/api/transaction", true,
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy("http://transaction-service:4080", true))
        );
        
        proxyController = new ProxyController();
        
        // Inject dependencies using reflection
        ReflectionTestUtils.setField(proxyController, "routes", testRoutes);
        ReflectionTestUtils.setField(proxyController, "restTemplate", restTemplate);
    }
    
    @Test
    void shouldProxyRequestToCorrectService() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/profile");
        
        ResponseEntity<String> mockResponse = ResponseEntity.ok("User profile data");
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer token123");
        
        // When
        ResponseEntity<String> response = proxyController.proxyRequest(
            null, HttpMethod.GET, request, headers);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User profile data", response.getBody());
        
        verify(restTemplate).exchange(
            eq(URI.create("http://user-service:4000/api/users/profile")),
            eq(HttpMethod.GET),
            any(),
            eq(String.class)
        );
    }
    
    @Test
    void shouldReturn404ForUnmatchedRoute() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/unknown/endpoint");
        
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        
        // When
        ResponseEntity<String> response = proxyController.proxyRequest(
            null, HttpMethod.GET, request, headers);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(restTemplate, never()).exchange(any(), any(), any(), any(Class.class));
    }
    
    @Test
    void shouldMatchMostSpecificRoute() {
        // Given - Request that could match both /api/users and /api/user
        when(request.getRequestURI()).thenReturn("/api/users/123");
        
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Specific user data");
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        
        // When
        ResponseEntity<String> response = proxyController.proxyRequest(
            null, HttpMethod.GET, request, headers);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Should match /api/users (more specific) not /api/user
        verify(restTemplate).exchange(
            eq(URI.create("http://user-service:4000/api/users/123")),
            eq(HttpMethod.GET),
            any(),
            eq(String.class)
        );
    }
    
    @Test
    void shouldAddCORSHeaders() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/profile");
        
        ResponseEntity<String> mockResponse = ResponseEntity.ok("User data");
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        
        // When
        ResponseEntity<String> response = proxyController.proxyRequest(
            null, HttpMethod.GET, request, headers);
        
        // Then
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Methods"));
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Headers"));
        
        assertEquals("*", response.getHeaders().getFirst("Access-Control-Allow-Origin"));
    }
    
    @Test
    void shouldHandlePostRequestWithBody() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users");
        
        String requestBody = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        ResponseEntity<String> mockResponse = ResponseEntity.status(HttpStatus.CREATED)
            .body("{\"id\":123,\"name\":\"John Doe\"}");
        
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        
        // When
        ResponseEntity<String> response = proxyController.proxyRequest(
            requestBody, HttpMethod.POST, request, headers);
        
        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains("John Doe"));
        
        verify(restTemplate).exchange(
            eq(URI.create("http://user-service:4000/api/users")),
            eq(HttpMethod.POST),
            any(),
            eq(String.class)
        );
    }
    
    @Test
    void shouldHandleProxyErrors() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/profile");
        
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Connection refused"));
        
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        
        // When
        ResponseEntity<String> response = proxyController.proxyRequest(
            null, HttpMethod.GET, request, headers);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Proxy error"));
        assertTrue(response.getBody().contains("Connection refused"));
    }
}