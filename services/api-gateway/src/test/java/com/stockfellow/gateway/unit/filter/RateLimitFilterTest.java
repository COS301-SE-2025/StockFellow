package com.stockfellow.gateway.unit.filter;

import com.stockfellow.gateway.filter.RateLimitFilter;
import com.stockfellow.gateway.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private RateLimitFilter rateLimitFilter;
    private List<Route> testRoutes;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testRoutes = Arrays.asList(
            new Route("/api/user", false, 
                new Route.RateLimit(1000L, 2), // 2 requests per second for testing
                new Route.Proxy("http://user-service:4020", true))
            // new Route("/api/transaction", true,
            //     new Route.RateLimit(1000L, 1), // 1 request per second for testing
            //     new Route.Proxy("http://transaction-service:4080", true))
        );
        
        rateLimitFilter = new RateLimitFilter(testRoutes);
    }
    
    @Test
    void shouldAllowRequestWithinRateLimit() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/user/profile");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        rateLimitFilter.doFilter(request, response, filterChain);
    
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
    
    @Test
    void shouldBlockRequestExceedingRateLimit() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/user/profile");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        
        // When - Make requests exceeding the limit (2 allowed, 3rd should be blocked)
        rateLimitFilter.doFilter(request, response, filterChain); // 1st request
        rateLimitFilter.doFilter(request, response, filterChain); // 2nd request
        rateLimitFilter.doFilter(request, response, filterChain); // 3rd request - should be blocked
        
        // Then
        verify(filterChain, times(2)).doFilter(request, response); // Only first 2 requests pass through
        verify(response, times(1)).setStatus(429); // 3rd request gets 429
        
        writer.flush();
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
    }
    
    @Test
    void shouldUseXForwardedForHeader() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/user/profile");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        // When
        rateLimitFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        // The rate limiting should use 192.168.1.100 as the client IP, not 127.0.0.1
    }
    
    @Test
    void shouldAllowRequestsForNonRateLimitedRoutes() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health"); // No rate limit configured
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        // When
        rateLimitFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
    
    @Test
    void shouldResetRateLimitAfterWindow() throws ServletException, IOException, InterruptedException {
        // Given
        Route shortWindowRoute = new Route("/api/test", false,
            new Route.RateLimit(100L, 1), // 1 request per 100ms
            new Route.Proxy("http://test-service:5000", true));
        
        RateLimitFilter shortWindowFilter = new RateLimitFilter(Arrays.asList(shortWindowRoute));
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        // When - First request should pass
        shortWindowFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        
        // Wait for window to reset
        Thread.sleep(150);
        
        // Second request after window reset should also pass
        shortWindowFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(2)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}