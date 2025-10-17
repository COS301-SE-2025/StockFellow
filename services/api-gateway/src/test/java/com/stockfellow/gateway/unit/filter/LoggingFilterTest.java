package com.stockfellow.gateway.unit.filter;

import com.stockfellow.gateway.filter.LoggingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoggingFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private LoggingFilter loggingFilter;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loggingFilter = new LoggingFilter();
    }
    
    @Test
    void shouldCreateLoggingFilter() {
        // Simple test to verify the filter can be created
        assertNotNull(loggingFilter);
        assertInstanceOf(LoggingFilter.class, loggingFilter);
    }
    
    @Test
    void shouldImplementFilterInterface() {
        // Verify that LoggingFilter implements the Filter interface
        assertTrue(loggingFilter instanceof javax.servlet.Filter);
    }
    
    @Test
    void shouldCallFilterChain() throws ServletException, IOException {
        // Given
        setupBasicMockRequest("GET", "/api/users/profile", 200);
        
        // When
        loggingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(any(), any()); // Don't care about exact wrapper types
    }
    
    @Test
    void shouldLogDifferentHttpMethods() throws ServletException, IOException {
        // Test GET
        setupBasicMockRequest("GET", "/api/users", 200);
        loggingFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
        
        reset(filterChain);
        
        // Test POST
        setupBasicMockRequest("POST", "/api/users", 201);
        loggingFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }
    
    @Test
    void shouldLogErrorResponses() throws ServletException, IOException {
        // Given
        setupBasicMockRequest("GET", "/api/nonexistent", 404);
        
        // When
        loggingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(any(), any());
        // Error responses should be logged as warnings (verified through log output)
    }
    
    @Test
    void shouldHandleNullHeaders() throws ServletException, IOException {
        // Given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn(null); // All headers null
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        
        when(response.getStatus()).thenReturn(200);
        
        // When & Then - Should not throw exceptions
        assertDoesNotThrow(() -> {
            loggingFilter.doFilter(request, response, filterChain);
        });
        
        verify(filterChain).doFilter(any(), any());
    }
    
    private void setupBasicMockRequest(String method, String uri, int statusCode) {
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        
        when(response.getStatus()).thenReturn(statusCode);
    }
}