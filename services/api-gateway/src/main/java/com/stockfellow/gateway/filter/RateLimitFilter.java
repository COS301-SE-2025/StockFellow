package com.stockfellow.gateway.filter;

import com.stockfellow.gateway.model.Route;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter implements Filter {
    
    private List<Route> routes;
    
    // In-memory storage for rate limiting (Will switch to Redis in production)
    private final Map<String, RateLimitBucket> clientBuckets = new ConcurrentHashMap<>();
    
    // Default constructor
    public RateLimitFilter() {
        this.routes = new ArrayList<>();
    }
    
    // Constructor for dependency injection
    public RateLimitFilter(List<Route> routes) {
        this.routes = routes;
    }
    
    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String clientId = getClientId(httpRequest);
    
        Route matchingRoute = findMatchingRoute(requestPath);
        
        if (matchingRoute != null && matchingRoute.getRateLimit() != null) {
            Route.RateLimit rateLimit = matchingRoute.getRateLimit();
            
            if (!isAllowed(clientId + ":" + requestPath, rateLimit)) {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
    
    private boolean isAllowed(String key, Route.RateLimit rateLimit) {
        RateLimitBucket bucket = clientBuckets.computeIfAbsent(key, 
            k -> new RateLimitBucket(rateLimit.getMax(), rateLimit.getWindowMs()));
        
        return bucket.tryConsume();
    }
    
    private String getClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private Route findMatchingRoute(String requestPath) {
        for (Route route : routes) {
            String routePattern = route.getUrl().replace("*", "");
            if (requestPath.startsWith(routePattern)) {
                return route;
            }
        }
        return null;
    }
    
    private static class RateLimitBucket {
        private final int maxRequests;
        private final long windowMs;
        private int requestCount;
        private long windowStart;
        
        public RateLimitBucket(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
            this.windowStart = System.currentTimeMillis();
            this.requestCount = 0;
        }
        
        public synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            
            // Reset window if expired
            if (now - windowStart >= windowMs) {
                windowStart = now;
                requestCount = 0;
            }
            
            if (requestCount < maxRequests) {
                requestCount++;
                return true;
            }
            
            return false;
        }
    }
}