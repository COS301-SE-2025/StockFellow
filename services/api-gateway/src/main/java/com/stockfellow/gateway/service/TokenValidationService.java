package com.stockfellow.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.gateway.model.TokenInfo;
import com.stockfellow.gateway.model.TokenValidationResult;
import com.stockfellow.gateway.service.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Duration;
import java.util.Set;

@Component
public class TokenValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenValidationService.class);

    private final KeycloakService keycloakService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Endpoints that don't require authentication
    private final Set<String> PUBLIC_ENDPOINTS = Set.of(
        "/api/auth/login",
        "/api/auth/verify-mfa",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/health",
        "/actuator/health",
        "/actuator/info",
        "/health",
        "/favicon.ico",
        "/error"
    );
    
    public TokenValidationService(KeycloakService keycloakService, 
                                RedisTemplate<String, String> redisTemplate,
                                ObjectMapper objectMapper) {
        this.keycloakService = keycloakService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    public TokenValidationResult validateRequest(String path, String authHeader) {
        logger.debug("Validating request for path: {}", path);
        logger.debug("Auth header present: {}", authHeader != null);

        // Skip validation for public endpoints
        if (isPublicEndpoint(path)) {
            logger.debug("Path {} is public, skipping validation", path);
            return TokenValidationResult.success();
        }
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid authorization header for path: {}", path);
            return TokenValidationResult.unauthorized("Missing or invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        logger.debug("Extracted token (first 20 chars): {}", token.substring(0, Math.min(20, token.length())));
        
        try {
            // First check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                return TokenValidationResult.unauthorized("Token has been revoked");
            }
            
            // Validate token with Keycloak
            TokenInfo tokenInfo = keycloakService.validateToken(token);
            
            if (tokenInfo.isValid()) {
                // Cache valid token info for faster subsequent requests
                cacheTokenInfo(token, tokenInfo);
                return TokenValidationResult.success(tokenInfo);
            } else if (tokenInfo.isExpired()) {
                return TokenValidationResult.expired("Access token has expired");
            } else {
                return TokenValidationResult.unauthorized("Invalid token");
            }
            
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return TokenValidationResult.unauthorized("Token validation failed");
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    private boolean isTokenBlacklisted(String token) {
        String key = "blacklisted_token:" + hashToken(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    private void cacheTokenInfo(String token, TokenInfo tokenInfo) {
        try {
            String key = "token_info:" + hashToken(token);
            String value = objectMapper.writeValueAsString(tokenInfo);
            // Cache for shorter duration than token expiry
            long cacheSeconds = Math.max(60, tokenInfo.getExpiresIn() - 300); // 5 min buffer
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(cacheSeconds));
        } catch (Exception e) {
            logger.warn("Failed to cache token info", e);
        }
    }
    
    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}