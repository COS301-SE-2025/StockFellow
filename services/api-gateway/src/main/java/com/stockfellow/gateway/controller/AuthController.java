package com.stockfellow.gateway.controller;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
public class AuthController {
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        System.out.println("Login endpoint hit");
        String loginUrl = String.format("%s/realms/%s/protocol/openid-connect/auth?client_id=%s&response_type=code&redirect_uri=%s",
            keycloakServerUrl, realm, clientId, "http://localhost:3000/auth/callback");
        response.sendRedirect(loginUrl);
    }
    
    @GetMapping("/register")
    public void register(HttpServletResponse response) throws IOException {
        System.out.println("Register endpoint hit");
        String registerUrl = String.format("%s/realms/%s/protocol/openid-connect/registrations?client_id=%s&response_type=code&redirect_uri=%s",
            keycloakServerUrl, realm, clientId, "http://localhost:3000/auth/callback");
        response.sendRedirect(registerUrl);
    }
    
    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        System.out.println("Logout endpoint hit");
        String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
            keycloakServerUrl, realm, "http://localhost:3000");
        response.sendRedirect(logoutUrl);
    }
    
    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login request received for user: " + loginRequest.getUsername());
            
            // Create Keycloak client for direct access grants
            Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm(realm)
                .clientId(clientId)
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .build();
            
            // Get access token
            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
            
            System.out.println("Login successful for user: " + loginRequest.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "access_token", tokenResponse.getToken(),
                "refresh_token", tokenResponse.getRefreshToken(),
                "expires_in", tokenResponse.getExpiresIn()
            ));
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "Authentication failed",
                    "details", e.getMessage()
                ));
        }
    }
    
    // DTO for login request
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}