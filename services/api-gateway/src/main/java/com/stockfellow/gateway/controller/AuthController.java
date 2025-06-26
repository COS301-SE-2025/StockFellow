
package com.stockfellow.gateway.controller;

import com.stockfellow.gateway.service.KeycloakService;
import com.stockfellow.gateway.service.TokenValidationService;
import com.stockfellow.gateway.model.RefreshTokenResponse;
import com.stockfellow.gateway.model.RefreshTokenRequest;
import com.stockfellow.gateway.model.TokenValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	 private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final KeycloakService keycloakService;
    private final TokenValidationService tokenValidationService;
	private final RedisTemplate<String, String> redisTemplate;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${app.keycloak.frontend.client-id}")
    private String frontendClientId;

    public AuthController(KeycloakService keycloakService, TokenValidationService tokenValidationService, RedisTemplate<String, String> redisTemplate) {
        this.keycloakService = keycloakService;
        this.tokenValidationService = tokenValidationService;
		this.redisTemplate = redisTemplate;
    }
    
    // Redirects to KC login page where there is forgot password and aditional features
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        System.out.println("Login endpoint hit");
        String loginUrl = String.format("%s/realms/%s/protocol/openid-connect/auth?client_id=%s&response_type=code&redirect_uri=%s",
            keycloakServerUrl, realm, frontendClientId, "http://localhost:3000/auth/callback");
        response.sendRedirect(loginUrl);
    }

    @GetMapping("/logout")
    public void logoutRedirect(HttpServletResponse response) throws IOException {
        System.out.println("Logout endpoint hit");
        String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
            keycloakServerUrl, realm, "http://localhost:3000");
        response.sendRedirect(logoutUrl);
    }
    
    @GetMapping("/register")
    public void register(HttpServletResponse response) throws IOException {
        System.out.println("Register endpoint hit");
        String registerUrl = String.format("%s/realms/%s/protocol/openid-connect/registrations?client_id=%s&response_type=code&redirect_uri=%s",
            keycloakServerUrl, realm, frontendClientId, "http://localhost:3000/auth/callback");
        response.sendRedirect(registerUrl);
    }
    
    // Uses direct access to sign in directly from custom login page
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login request received for user: " + loginRequest.getUsername());

            // Validate input
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required"));
            }

            Map<String, Object> tokenResponse = keycloakService.authenticateUser(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );
            
            if (tokenResponse.containsKey("error")) {
                System.err.println("Login failed for user: " + loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenResponse);
            }
            
            System.out.println("Login successful for user: " + loginRequest.getUsername());
            return ResponseEntity.ok(tokenResponse);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Authentication service unavailable",
                    "details", e.getMessage()
                ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Registration request received for user: " + registerRequest.getUsername());
            
            // Validate input
            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null || 
                registerRequest.getEmail() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username, email, and password are required"));
            }
            
            // Use KeycloakService for user registration
            Map<String, Object> registrationResponse = keycloakService.registerUser(registerRequest);
            
            if (registrationResponse.containsKey("error")) {
                String error = (String) registrationResponse.get("error");
                if ("User already exists".equals(error)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(registrationResponse);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registrationResponse);
                }
            }
            
            System.out.println("User registered successfully: " + registerRequest.getUsername());
            return ResponseEntity.ok(registrationResponse);
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Registration service unavailable",
                    "details", e.getMessage()
                ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "refresh_token_required", 
                               "message", "Refresh token is required"));
            }
            
            RefreshTokenResponse refreshResponse = keycloakService.refreshToken(request.getRefreshToken());
            
            if (refreshResponse.isSuccess()) {
                // If there's an original request to retry, include it in response
                Map<String, Object> response = new HashMap<>();
                response.put("access_token", refreshResponse.getAccessToken());
                response.put("refresh_token", refreshResponse.getRefreshToken());
                response.put("expires_in", refreshResponse.getExpiresIn());
                response.put("token_type", refreshResponse.getTokenType());
                
                // If the client wants to retry original request
                if (request.getRetryOriginalRequest() != null && request.getRetryOriginalRequest()) {
                    response.put("retry_original_request", true);
                }
                
                return ResponseEntity.ok(response);
                
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "refresh_failed", 
                               "message", refreshResponse.getError()));
            }
            
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "server_error", 
                           "message", "Token refresh failed"));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            TokenValidationResult result = tokenValidationService.validateRequest("/api/protected", authHeader);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user_id", result.getTokenInfo().getUserId(),
                    "username", result.getTokenInfo().getUsername(),
                    "roles", result.getTokenInfo().getRoles()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", result.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("valid", false, "message", "Validation failed"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
				
				keycloakService.logoutUser(token);
            }
            
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
            
        } catch (Exception e) {
            logger.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "logout_failed", "message", "Logout failed"));
        }
    }
	
	private void blacklistToken(String token) {
        // Add to Redis blacklist with expiration
        String key = "blacklisted_token:" + DigestUtils.sha256Hex(token);
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(24));
    }

    // DTOs
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

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
    
    public static class RefreshTokenRequest {
        private String refreshToken;
        private Boolean retryOriginalRequest;
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public Boolean getRetryOriginalRequest() { return retryOriginalRequest; }
        public void setRetryOriginalRequest(Boolean retryOriginalRequest) { this.retryOriginalRequest = retryOriginalRequest; }
    }
}