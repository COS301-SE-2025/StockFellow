package com.stockfellow.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.gateway.controller.AuthController.RegisterRequest;
import com.stockfellow.gateway.model.RefreshTokenResponse;
import com.stockfellow.gateway.model.TokenInfo;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.codec.digest.DigestUtils;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.*;

@Service
public class KeycloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.gateway.client-id}")
    private String gatewayClientId;

    @Value("${app.keycloak.gateway.client-secret}")
    private String gatewayClientSecret;

    @Value("${app.keycloak.frontend.client-id}")
    private String frontendClientId;

    @Value("${app.keycloak.admin.username}")
    private String adminUsername;

    @Value("${app.keycloak.admin.password}")
    private String adminPassword;

    @Value("${app.keycloak.admin.realm}")
    private String adminRealm;

    @Value("${app.keycloak.admin.client-id}")
    private String adminClientId;

    // Add MFA service configuration
    @Value("http://mfa-service:8087")
    private String mfaServiceUrl;

    @Value("${app.mfa.enabled:true}")
    private boolean mfaEnabled;

    public KeycloakService(RestTemplate restTemplate,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // Authenticate user with username/password
    public Map<String, Object> authenticateUser(String username, String password) {
        try {
            // Create Keycloak client for direct access grants
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .clientId(frontendClientId)
                    .username(username)
                    .password(password)
                    .build();

            // Get access token
            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();

            return Map.of(
                    "access_token", tokenResponse.getToken(),
                    "refresh_token", tokenResponse.getRefreshToken(),
                    "expires_in", tokenResponse.getExpiresIn(),
                    "token_type", "Bearer");

        } catch (Exception e) {
            logger.error("Authentication failed for user: " + username, e);
            return Map.of("error", "Authentication failed", "details", e.getMessage());
        }
    }

    // TEST METHOD: Exact duplicate of authenticateUser for test endpoint
    // This method will remain unchanged for testing purposes
    public Map<String, Object> authenticateUserForTesting(String username, String password) {
        try {
            // Create Keycloak client for direct access grants
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .clientId(frontendClientId)
                    .username(username)
                    .password(password)
                    .build();

            // Get access token
            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();

            return Map.of(
                    "access_token", tokenResponse.getToken(),
                    "refresh_token", tokenResponse.getRefreshToken(),
                    "expires_in", tokenResponse.getExpiresIn(),
                    "token_type", "Bearer");

        } catch (Exception e) {
            logger.error("TEST Authentication failed for user: " + username, e);
            return Map.of("error", "Authentication failed", "details", e.getMessage());
        }
    }

    // Register new user
    public Map<String, Object> registerUser(RegisterRequest registerRequest) {
        try {
            // Create admin client to manage users
            Keycloak adminClient = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(adminRealm)
                    .clientId(adminClientId)
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

                // Clear required actions
                UserResource userResource = usersResource.get(userId);
                UserRepresentation user = userResource.toRepresentation();
                user.setRequiredActions(new ArrayList<>());
                userResource.update(user);

                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(registerRequest.getPassword());
                credential.setTemporary(false);

                usersResource.get(userId).resetPassword(credential);

                return Map.of(
                        "message", "User registered successfully",
                        "username", registerRequest.getUsername(),
                        "userId", userId);

            } else if (response.getStatus() == 409) {
                return Map.of("error", "User already exists");
            } else {
                return Map.of("error", "Registration failed");
            }

        } catch (Exception e) {
            logger.error("Registration failed for user: " + registerRequest.getUsername(), e);
            return Map.of("error", "Registration service unavailable", "details", e.getMessage());
        }
    }

    // Validate token using Keycloak introspection
    public TokenInfo validateToken(String accessToken) {
        try {
            // First check cache
            String cacheKey = "token_info:" + DigestUtils.sha256Hex(accessToken);
            String cachedInfo = redisTemplate.opsForValue().get(cacheKey);

            if (cachedInfo != null) {
                TokenInfo tokenInfo = objectMapper.readValue(cachedInfo, TokenInfo.class);
                if (tokenInfo.getExpiresIn() > System.currentTimeMillis() / 1000) {
                    return tokenInfo;
                }
            }

            // Validate with Keycloak introspection endpoint
            String introspectionUrl = String.format("%s/realms/%s/protocol/openid-connect/token/introspect",
                    keycloakServerUrl, realm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            if (gatewayClientSecret != null && !gatewayClientSecret.isEmpty()) {
                headers.setBasicAuth(gatewayClientId, gatewayClientSecret);
            }

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", accessToken);
            body.add("token_type_hint", "access_token");
            if (gatewayClientSecret == null || gatewayClientSecret.isEmpty()) {
                body.add("client_id", gatewayClientId);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(introspectionUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TokenInfo tokenInfo = parseIntrospectionResponse(response.getBody());

                // Cache valid token info
                if (tokenInfo.isValid()) {
                    cacheTokenInfo(accessToken, tokenInfo);
                }

                return tokenInfo;
            } else {
                return TokenInfo.invalid();
            }

        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return TokenInfo.invalid();
        }
    }

    public RefreshTokenResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            if (gatewayClientSecret != null && !gatewayClientSecret.isEmpty()) {
                headers.setBasicAuth(gatewayClientId, gatewayClientSecret);
            }

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", refreshToken);
            if (gatewayClientSecret == null || gatewayClientSecret.isEmpty()) {
                body.add("client_id", gatewayClientId);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                return RefreshTokenResponse.builder()
                        .accessToken((String) tokenData.get("access_token"))
                        .refreshToken((String) tokenData.get("refresh_token"))
                        .expiresIn((Integer) tokenData.get("expires_in"))
                        .tokenType((String) tokenData.get("token_type"))
                        .success(true)
                        .build();
            } else {
                return RefreshTokenResponse.failure("Failed to refresh token");
            }

        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return RefreshTokenResponse.failure("Token refresh failed: " + e.getMessage());
        }
    }

    // Logout user by blacklisting token
    public void logoutUser(String accessToken) {
        try {
            // Add token to blacklist
            String key = "blacklisted_token:" + DigestUtils.sha256Hex(accessToken);
            redisTemplate.opsForValue().set(key, "true", Duration.ofHours(24));

            // Remove from cache
            String cacheKey = "token_info:" + DigestUtils.sha256Hex(accessToken);
            redisTemplate.delete(cacheKey);

            logger.info("User logged out successfully");

        } catch (Exception e) {
            logger.error("Logout failed", e);
        }
    }

    // Check if token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklisted_token:" + DigestUtils.sha256Hex(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void cacheTokenInfo(String token, TokenInfo tokenInfo) {
        try {
            String key = "token_info:" + DigestUtils.sha256Hex(token);
            String value = objectMapper.writeValueAsString(tokenInfo);
            // Cache for shorter duration than token expiry
            long cacheSeconds = Math.max(60, tokenInfo.getExpiresIn() - 300); // 5 min buffer
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(cacheSeconds));
        } catch (Exception e) {
            logger.warn("Failed to cache token info", e);
        }
    }

    private TokenInfo parseIntrospectionResponse(Map<String, Object> introspectionData) {
        boolean active = Boolean.TRUE.equals(introspectionData.get("active"));

        if (!active) {
            return TokenInfo.invalid();
        }

        Long exp = getLongValue(introspectionData, "exp");
        long currentTime = System.currentTimeMillis() / 1000;

        if (exp != null && exp < currentTime) {
            return TokenInfo.expired();
        }

        String userId = (String) introspectionData.get("sub");
        String username = (String) introspectionData.get("preferred_username");

        // Extract roles from realm_access and resource_access
        Set<String> roles = extractRoles(introspectionData);

        return TokenInfo.builder()
                .valid(true)
                .expired(false)
                .userId(userId)
                .username(username)
                .roles(roles)
                .expiresIn(exp != null ? exp - currentTime : 0)
                .claims(introspectionData)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Map<String, Object> introspectionData) {
        Set<String> roles = new HashSet<>();

        // Extract realm roles
        Map<String, Object> realmAccess = (Map<String, Object>) introspectionData.get("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                roles.addAll(realmRoles);
            }
        }

        // Extract resource roles
        Map<String, Object> resourceAccess = (Map<String, Object>) introspectionData.get("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().forEach(resource -> {
                if (resource instanceof Map) {
                    Map<String, Object> resourceMap = (Map<String, Object>) resource;
                    List<String> resourceRoles = (List<String>) resourceMap.get("roles");
                    if (resourceRoles != null) {
                        roles.addAll(resourceRoles);
                    }
                }
            });
        }

        return roles;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    // New method for authentication with MFA support
    public Map<String, Object> authenticateUserWithMFA(String username, String password) {
        try {
            // First authenticate with Keycloak
            Map<String, Object> authResult = authenticateUser(username, password);

            if (authResult.containsKey("access_token") && mfaEnabled) {
                // Authentication successful, now trigger MFA
                String email = getUserEmailFromToken((String) authResult.get("access_token"));

                if (email != null) {
                    // Call MFA service to send OTP
                    boolean mfaTriggered = triggerMFA(email, username);

                    if (mfaTriggered) {
                        // Store temp token in Redis for MFA verification
                        String tempTokenKey = "mfa_temp:" + email;
                        String tokenData = objectMapper.writeValueAsString(authResult);
                        redisTemplate.opsForValue().set(tempTokenKey, tokenData, Duration.ofMinutes(10));

                        // Return response indicating MFA is required
                        return Map.of(
                                "mfa_required", true,
                                "message", "Please check your email for verification code",
                                "email", email,
                                "temp_session", generateTempSession(email));
                    }
                }
            }

            return authResult;

        } catch (Exception e) {
            logger.error("Authentication with MFA failed for user: " + username, e);
            return Map.of("error", "Authentication failed", "details", e.getMessage());
        }
    }

    // Method to complete MFA verification
    public Map<String, Object> completeMFAVerification(String email, String otpCode, String tempSession) {
        try {
            // Verify the temp session
            if (!verifyTempSession(email, tempSession)) {
                return Map.of("error", "Invalid or expired session");
            }

            // Verify OTP with MFA service
            boolean otpValid = verifyOTPWithMFAService(email, otpCode);

            if (otpValid) {
                // Retrieve stored tokens
                String tempTokenKey = "mfa_temp:" + email;
                String tokenData = redisTemplate.opsForValue().get(tempTokenKey);

                if (tokenData != null) {
                    // Clean up temp data
                    redisTemplate.delete(tempTokenKey);
                    redisTemplate.delete("temp_session:" + email);

                    // Return the original auth tokens
                    Map<String, Object> authResult = objectMapper.readValue(tokenData, Map.class);
                    authResult.put("mfa_verified", true);
                    return authResult;
                }
            }

            return Map.of("error", "Invalid verification code");

        } catch (Exception e) {
            logger.error("MFA verification failed for email: " + email, e);
            return Map.of("error", "MFA verification failed", "details", e.getMessage());
        }
    }

    // Helper method to trigger MFA
    private boolean triggerMFA(String email, String userId) {
        try {
            String mfaUrl = mfaServiceUrl + "/api/mfa/send-otp";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> mfaRequest = Map.of(
                    "email", email,
                    "userId", userId);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(mfaRequest, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(mfaUrl, request, Map.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            logger.error("Failed to trigger MFA for email: " + email, e);
            return false;
        }
    }

    // Helper method to verify OTP with MFA service
    private boolean verifyOTPWithMFAService(String email, String otpCode) {
        try {
            String verifyUrl = mfaServiceUrl + "/api/mfa/verify-otp";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> verifyRequest = Map.of(
                    "email", email,
                    "otpCode", otpCode);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(verifyRequest, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Boolean.TRUE.equals(response.getBody().get("success"));
            }

            return false;

        } catch (Exception e) {
            logger.error("Failed to verify OTP for email: " + email, e);
            return false;
        }
    }

    // Helper method to extract email from token
    private String getUserEmailFromToken(String accessToken) {
        try {
            TokenInfo tokenInfo = validateToken(accessToken);
            if (tokenInfo.isValid() && tokenInfo.getClaims() != null) {
                return (String) tokenInfo.getClaims().get("email");
            }
        } catch (Exception e) {
            logger.error("Failed to extract email from token", e);
        }
        return null;
    }

    // Helper method to generate temp session
    private String generateTempSession(String email) {
        String tempSession = "temp_" + email + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set("temp_session:" + email, tempSession, Duration.ofMinutes(10));
        return tempSession;
    }

    // Helper method to verify temp session
    private boolean verifyTempSession(String email, String tempSession) {
        String storedSession = redisTemplate.opsForValue().get("temp_session:" + email);
        return tempSession.equals(storedSession);
    }
}