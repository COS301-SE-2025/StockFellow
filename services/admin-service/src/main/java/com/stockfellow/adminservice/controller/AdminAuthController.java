package com.stockfellow.adminservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm:stockfellow}")
    private String realm;

    @Value("${keycloak.resource:admin-service-client}")
    private String clientId;

    @Value("${keycloak.credentials.secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Admin login attempt for username: {}", loginRequest.getUsername());

            // Validate input
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Username is required"
                ));
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Password is required"
                ));
            }

            // Construct Keycloak token endpoint URL
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", 
                                           keycloakUrl, realm);

            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Prepare request body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("username", loginRequest.getUsername());
            body.add("password", loginRequest.getPassword());
            
            // Add client secret if configured
            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                body.add("client_secret", clientSecret);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            logger.debug("Sending authentication request to Keycloak: {}", tokenUrl);

            // Make request to Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                
                logger.info("Successful admin login for username: {}", loginRequest.getUsername());
                
                // Return the token response with additional metadata
                return ResponseEntity.ok(Map.of(
                    "access_token", tokenResponse.get("access_token"),
                    "token_type", tokenResponse.getOrDefault("token_type", "Bearer"),
                    "expires_in", tokenResponse.getOrDefault("expires_in", 300),
                    "refresh_token", tokenResponse.getOrDefault("refresh_token", ""),
                    "scope", tokenResponse.getOrDefault("scope", ""),
                    "login_time", java.time.Instant.now().toString(),
                    "user_type", "admin"
                ));
            } else {
                logger.warn("Unexpected response from Keycloak: status={}, body={}", 
                          response.getStatusCode(), response.getBody());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Authentication failed",
                    "message", "Invalid credentials or authentication server error"
                ));
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Authentication failed for username {}: {} - {}", 
                        loginRequest.getUsername(), e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Authentication failed",
                    "message", "Invalid username or password"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Authentication error",
                    "message", "Please check your credentials and try again"
                ));
            }
        } catch (Exception e) {
            logger.error("Error during admin login for username {}: {}", 
                        loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", "Authentication service temporarily unavailable"
            ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshRequest) {
        try {
            logger.info("Admin token refresh attempt");

            if (refreshRequest.getRefreshToken() == null || refreshRequest.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Refresh token is required"
                ));
            }

            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", 
                                           keycloakUrl, realm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("refresh_token", refreshRequest.getRefreshToken());

            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                body.add("client_secret", clientSecret);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                
                logger.info("Successful admin token refresh");
                
                return ResponseEntity.ok(Map.of(
                    "access_token", tokenResponse.get("access_token"),
                    "token_type", tokenResponse.getOrDefault("token_type", "Bearer"),
                    "expires_in", tokenResponse.getOrDefault("expires_in", 300),
                    "refresh_token", tokenResponse.getOrDefault("refresh_token", refreshRequest.getRefreshToken()),
                    "scope", tokenResponse.getOrDefault("scope", ""),
                    "refreshed_at", java.time.Instant.now().toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Token refresh failed",
                    "message", "Invalid refresh token"
                ));
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Token refresh failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Token refresh failed",
                "message", "Invalid or expired refresh token"
            ));
        } catch (Exception e) {
            logger.error("Error during token refresh: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", "Token refresh service temporarily unavailable"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
        try {
            logger.info("Admin logout attempt");

            if (logoutRequest.getRefreshToken() == null || logoutRequest.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Refresh token is required for logout"
                ));
            }

            String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout", 
                                            keycloakUrl, realm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("refresh_token", logoutRequest.getRefreshToken());

            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                body.add("client_secret", clientSecret);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(logoutUrl, request, Void.class);

            logger.info("Admin logout successful");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful",
                "logged_out_at", java.time.Instant.now().toString()
            ));

        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            // Even if logout fails on Keycloak side, we can still return success
            // since the client should discard the tokens anyway
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout completed (tokens should be discarded)",
                "logged_out_at", java.time.Instant.now().toString()
            ));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Invalid token",
                    "message", "Authorization header missing or invalid"
                ));
            }

            // The JWT validation will be handled by Spring Security
            // If we reach here, the token is valid
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Token is valid",
                "validated_at", java.time.Instant.now().toString()
            ));

        } catch (Exception e) {
            logger.error("Error during token validation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Token validation failed",
                "message", "Invalid or expired token"
            ));
        }
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

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class LogoutRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}