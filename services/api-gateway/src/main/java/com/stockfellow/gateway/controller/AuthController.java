package com.stockfellow.gateway.controller;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
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
    
    @Value("${app.keycloak.admin.username}")
    private String adminUsername;
    
    @Value("${app.keycloak.admin.password}")
    private String adminPassword;
    
    @Value("${app.keycloak.admin.realm}")
    private String adminRealm;
    
    @Value("${app.keycloak.admin.client-id}")
    private String adminClientId;
    
    // Redirects to KC login page where there is forgot password and aditional features
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
    
    // Uses direct access to sign in directly from custom login page
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

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Registration request received for user: " + registerRequest.getUsername());
            
            // Validate input
            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null || 
                registerRequest.getEmail() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username, email, and password are required"));
            }
            
            // Create admin client to manage users
            Keycloak adminClient = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
            
            // Get realm resource
            RealmResource realmResource = adminClient.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            // Create user representation
            UserRepresentation newUser = new UserRepresentation();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setEnabled(true);
            newUser.setEmailVerified(true);
            
            // Create user
            Response response = usersResource.create(newUser);
            
            if (response.getStatus() == 201) {
                // User created successfully, now set password
                String userId = CreatedResponseUtil.getCreatedId(response);
                
                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(registerRequest.getPassword());
                credential.setTemporary(false); // Set to true if you want user to change password on first login
                
                usersResource.get(userId).resetPassword(credential);
                
                System.out.println("User registered successfully: " + registerRequest.getUsername());
                
                return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "username", registerRequest.getUsername(),
                    "userId", userId
                ));
                
            } else if (response.getStatus() == 409) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "User already exists"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Registration failed"));
            }
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Registration service unavailable",
                    "details", e.getMessage()
                ));
        }
    }

    // DTO for registration request
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
}