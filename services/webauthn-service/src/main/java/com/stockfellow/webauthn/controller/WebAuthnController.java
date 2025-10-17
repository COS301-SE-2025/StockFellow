package com.stockfellow.webauthn.controller;

import com.stockfellow.webauthn.dto.*;
import com.stockfellow.webauthn.entity.KeycloakWebAuthnCredential;
import com.stockfellow.webauthn.service.WebAuthnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/webauthn")
@CrossOrigin(origins = "*")
public class WebAuthnController {

    private static final Logger logger = LoggerFactory.getLogger(WebAuthnController.class);

    private final WebAuthnService webAuthnService;

    public WebAuthnController(WebAuthnService webAuthnService) {
        this.webAuthnService = webAuthnService;
    }

    // Registration

    @PostMapping("/register/start")
    public ResponseEntity<ApiResponse<RegistrationStartResponse>> startRegistration(
            @Valid @RequestBody RegistrationStartRequest request) {

        logger.info("WebAuthn registration start requested for user: {}", request.getUsername());

        try {
            RegistrationStartResponse response = webAuthnService.startRegistration(request);
            return ResponseEntity.ok(ApiResponse.success("Registration challenge generated", response));
        } catch (Exception e) {
            logger.error("Failed to start registration for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to start registration: " + e.getMessage()));
        }
    }

    @PostMapping("/register/complete/{challenge}")
    public ResponseEntity<ApiResponse<String>> completeRegistration(
            @PathVariable String challenge,
            @Valid @RequestBody RegistrationCompleteRequest request) {

        logger.info("WebAuthn registration completion requested for credential: {}", request.getCredentialId());

        try {
            ApiResponse<String> response = webAuthnService.completeRegistration(challenge, request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Failed to complete registration for credential: {}", request.getCredentialId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    // Authentication
    @PostMapping("/authenticate/start")
    public ResponseEntity<ApiResponse<AuthenticationStartResponse>> startAuthentication(
            @Valid @RequestBody AuthenticationStartRequest request) {

        logger.info("WebAuthn authentication start requested for user: {}", request.getUsername());

        try {
            AuthenticationStartResponse response = webAuthnService.startAuthentication(request);
            return ResponseEntity.ok(ApiResponse.success("Authentication challenge generated", response));
        } catch (Exception e) {
            logger.error("Failed to start authentication for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to start authentication: " + e.getMessage()));
        }
    }

    @PostMapping("/authenticate/complete/{challenge}")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> completeAuthentication(
            @PathVariable String challenge,
            @Valid @RequestBody AuthenticationCompleteRequest request) {

        logger.info("WebAuthn authentication completion requested for credential: {}", request.getCredentialId());

        try {
            ApiResponse<AuthenticationResponse> response = webAuthnService.completeAuthentication(challenge, request);
            if (response.isSuccess()) {
                logger.info("User successfully authenticated via WebAuthn: {}", response.getData().getUsername());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Failed to complete authentication for credential: {}", request.getCredentialId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }

    // credential management
    @GetMapping("/credentials/{userId}")
    public ResponseEntity<ApiResponse<List<KeycloakWebAuthnCredential>>> getUserCredentials(
            @PathVariable String userId) {

        logger.info("Fetching credentials for user: {}", userId);

        try {
            List<KeycloakWebAuthnCredential> credentials = webAuthnService.getUserCredentials(userId);
            return ResponseEntity.ok(ApiResponse.success("Credentials retrieved", credentials));
        } catch (Exception e) {
            logger.error("Failed to fetch credentials for user: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch credentials: " + e.getMessage()));
        }
    }

    // check if user has credentials
    @GetMapping("/credentials/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasCredentials(
            @PathVariable String userId) {

        try {
            boolean hasCredentials = webAuthnService.hasCredentials(userId);
            return ResponseEntity.ok(ApiResponse.success("Credential check completed", hasCredentials));
        } catch (Exception e) {
            logger.error("Failed to check credentials for user: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to check credentials: " + e.getMessage()));
        }
    }

    // delete
    @DeleteMapping("/credentials/{userId}/{credentialId}")
    public ResponseEntity<ApiResponse<String>> deleteCredential(
            @PathVariable String userId,
            @PathVariable String credentialId) {

        logger.info("Deleting credential {} for user: {}", credentialId, userId);

        try {
            boolean deleted = webAuthnService.deleteCredential(userId, credentialId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Credential deleted successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Credential not found or access denied"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete credential {} for user: {}", credentialId, userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete credential: " + e.getMessage()));
        }
    }

    // health check
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("WebAuthn service is running"));
    }
}
