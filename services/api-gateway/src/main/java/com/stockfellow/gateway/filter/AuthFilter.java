package com.stockfellow.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.gateway.service.TokenValidationService;
import com.stockfellow.gateway.model.TokenInfo;
import com.stockfellow.gateway.model.TokenValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    
    private final TokenValidationService tokenValidationService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Paths that should bypass authentication (webhooks, health checks, etc.)
    private static final List<String> UNAUTHENTICATED_PATHS = Arrays.asList(
        "/api/transaction/webhook/**",      // PSP webhook callback
        "/api/transaction/psp-callback/**", // Alternative webhook path
        "/actuator/**",                      // Health checks
        "/health/**"                         // Additional health endpoints
    );
    
    public AuthFilter(TokenValidationService tokenValidationService, ObjectMapper objectMapper) {
        this.tokenValidationService = tokenValidationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String authHeader = httpRequest.getHeader("Authorization");
        
        logger.debug("Processing request: {} {}", httpRequest.getMethod(), path);
        
        // Skip authentication for webhook and other unauthenticated paths
        if (isUnauthenticatedPath(path)) {
            logger.debug("Bypassing authentication for path: {}", path);
            chain.doFilter(request, response);
            return;
        }
        
        // Validate token
        TokenValidationResult validationResult = tokenValidationService.validateRequest(path, authHeader);
        
        if (validationResult.isSuccess()) {
            HttpServletRequest enrichedRequest = 
                enrichRequestWithUserInfo(httpRequest, validationResult.getTokenInfo());
            chain.doFilter(enrichedRequest, response);
            
        } else if (validationResult.isExpired()) {
            sendTokenExpiredResponse(httpResponse);
            
        } else {
            sendUnauthorizedResponse(httpResponse, validationResult.getMessage());
        }
    }
    
    /**
     * Check if the path should bypass authentication
     */
    private boolean isUnauthenticatedPath(String path) {
        return UNAUTHENTICATED_PATHS.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private HttpServletRequest enrichRequestWithUserInfo(HttpServletRequest request, TokenInfo tokenInfo) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                // Add user context headers for downstream services
                switch (name.toLowerCase()) {
                    case "x-user-id":
                        return tokenInfo != null ? tokenInfo.getUserId() : null;
                    case "x-username":
                        return tokenInfo != null ? tokenInfo.getUsername() : null;
                    case "x-user-roles":
                        return tokenInfo != null && tokenInfo.getRoles() != null 
                            ? String.join(",", tokenInfo.getRoles()) : null;
                    default:
                        return super.getHeader(name);
                }
            }
            
            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> headers = new HashSet<>();
                Enumeration<String> originalHeaders = super.getHeaderNames();
                while (originalHeaders.hasMoreElements()) {
                    headers.add(originalHeaders.nextElement());
                }
                // Add our custom headers
                if (tokenInfo != null) {
                    headers.add("X-User-Id");
                    headers.add("X-Username");
                    headers.add("X-User-Roles");
                }
                return Collections.enumeration(headers);
            }
        };
    }
    
    private void sendTokenExpiredResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        
        Map<String, Object> errorResponse = Map.of(
            "error", "token_expired",
            "message", "Access token has expired",
            "action", "refresh_token",
            "timestamp", Instant.now().toString()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
    
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        
        Map<String, Object> errorResponse = Map.of(
            "error", "unauthorized",
            "message", message != null ? message : "Authentication required",
            "timestamp", Instant.now().toString()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}