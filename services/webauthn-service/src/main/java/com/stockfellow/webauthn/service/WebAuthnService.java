package com.stockfellow.webauthn.service;

import com.stockfellow.webauthn.dto.*;
import com.stockfellow.webauthn.entity.WebAuthnChallenge;
import com.stockfellow.webauthn.entity.KeycloakWebAuthnCredential;
import com.stockfellow.webauthn.repository.WebAuthnChallengeRepository;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.Base64UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WebAuthnService {

    private static final Logger logger = LoggerFactory.getLogger(WebAuthnService.class);

    private final WebAuthnChallengeRepository challengeRepository;
    private final KeycloakService keycloakService;
    private final JwtService jwtService;
    private final KeycloakWebAuthnService keycloakWebAuthnService;
    private final WebAuthnManager webAuthnManager;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${webauthn.rpId}")
    private String rpId;

    @Value("${webauthn.rpName}")
    private String rpName;

    @Value("${webauthn.origin}")
    private String origin;

    @Value("${webauthn.timeout:60000}")
    private long timeout;

    public WebAuthnService(WebAuthnChallengeRepository challengeRepository,
            KeycloakService keycloakService,
            JwtService jwtService,
            KeycloakWebAuthnService keycloakWebAuthnService) {
        this.challengeRepository = challengeRepository;
        this.keycloakService = keycloakService;
        this.jwtService = jwtService;
        this.keycloakWebAuthnService = keycloakWebAuthnService;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    }

    public RegistrationStartResponse startRegistration(RegistrationStartRequest request) {
        logger.info("Starting WebAuthn registration for user: {} ({})", request.getUsername(), request.getUserId());

        try {

            if (!keycloakService.userExists(request.getUserId())) {
                throw new RuntimeException("User not found in Keycloak: " + request.getUserId());
            }

            // Generate a cryptographically secure challenge
            byte[] challengeBytes = new byte[32];
            secureRandom.nextBytes(challengeBytes);
            String challenge = Base64UrlUtil.encodeToString(challengeBytes);

            // Store the challenge in database -> expires in 5 min
            WebAuthnChallenge challengeEntity = new WebAuthnChallenge(
                    challenge,
                    request.getUserId(),
                    request.getUsername(),
                    WebAuthnChallenge.ChallengeType.REGISTRATION,
                    LocalDateTime.now().plusMinutes(5));
            challengeRepository.save(challengeEntity);

            // Create the registration response for frontend
            RegistrationStartResponse response = new RegistrationStartResponse();
            response.setChallenge(challenge);
            response.setRpId(rpId);
            response.setRpName(rpName);
            response.setTimeout(timeout);
            response.setAttestation("none");

            response.setUser(new RegistrationStartResponse.UserInfo(
                    Base64UrlUtil.encodeToString(request.getUserId().getBytes()),
                    request.getUsername(),
                    request.getUsername()));

            response.setPubKeyCredParams(new RegistrationStartResponse.PubKeyCredParams[] {
                    new RegistrationStartResponse.PubKeyCredParams("public-key", -7),
                    new RegistrationStartResponse.PubKeyCredParams("public-key", -257)
            });

            response.setAuthenticatorSelection(new RegistrationStartResponse.AuthenticatorSelection(
                    "platform",
                    "required",
                    false));

            logger.info("Successfully generated registration challenge for user: {}", request.getUsername());
            return response;

        } catch (Exception e) {
            logger.error("Failed to start registration for user: {}", request.getUsername(), e);
            throw new RuntimeException("Failed to start registration: " + e.getMessage());
        }
    }

    public ApiResponse<String> completeRegistration(String challenge, RegistrationCompleteRequest request) {
        logger.info("Completing WebAuthn registration for credential: {}", request.getCredentialId());

        try {

            Optional<WebAuthnChallenge> challengeOpt = challengeRepository
                    .findByChallengeAndIsUsedFalseAndExpiresAtAfter(challenge, LocalDateTime.now());

            if (!challengeOpt.isPresent()) {
                return ApiResponse.error("Invalid or expired challenge");
            }

            WebAuthnChallenge challengeEntity = challengeOpt.get();

            challengeEntity.setIsUsed(true);
            challengeRepository.save(challengeEntity);

            // Parse the registration response using WebAuthn4J library
            // This verifies the cryptographic signature and extracts the public key
            byte[] clientDataJSON = Base64UrlUtil.decode(request.getClientDataJSON());
            byte[] attestationObject = Base64UrlUtil.decode(request.getAttestationObject());

            RegistrationRequest registrationRequest = new RegistrationRequest(
                    attestationObject,
                    clientDataJSON);

            RegistrationParameters registrationParameters = new RegistrationParameters(
                    new ServerProperty(new Origin(origin), rpId, new DefaultChallenge(challenge), null),
                    null,
                    false,
                    true);

            RegistrationData registrationData = webAuthnManager.parse(registrationRequest);
            webAuthnManager.validate(registrationData, registrationParameters);

            // Extract and store the credential in Keycloak database

            byte[] credentialId = registrationData.getAttestationObject().getAuthenticatorData()
                    .getAttestedCredentialData().getCredentialId();
            String credentialIdBase64 = Base64.getEncoder().encodeToString(credentialId);

            String publicKeyBase64 = credentialIdBase64;

            keycloakWebAuthnService.storeWebAuthnCredential(
                    challengeEntity.getUserId(),
                    request.getCredentialId(),
                    publicKeyBase64,
                    request.getAuthenticatorName(),
                    0L);

            logger.info("Successfully registered WebAuthn credential for user: {}", challengeEntity.getUsername());
            return ApiResponse.success("Biometric authentication registered successfully");

        } catch (Exception e) {
            logger.error("Failed to complete registration for credential: {}", request.getCredentialId(), e);
            return ApiResponse.error("Registration failed: " + e.getMessage());
        }
    }

    public AuthenticationStartResponse startAuthentication(AuthenticationStartRequest request) {
        logger.info("Starting WebAuthn authentication for user: {}", request.getUsername());

        try {

            String userId = keycloakService.getUserIdByUsername(request.getUsername());
            if (userId == null) {
                throw new RuntimeException("User not found: " + request.getUsername());
            }

            if (!keycloakWebAuthnService.hasWebAuthnCredentials(userId)) {
                throw new RuntimeException("No biometric credentials found for user: " + request.getUsername());
            }

            // Generate challenge
            byte[] challengeBytes = new byte[32];
            secureRandom.nextBytes(challengeBytes);
            String challenge = Base64UrlUtil.encodeToString(challengeBytes);

            // Store challenge
            WebAuthnChallenge challengeEntity = new WebAuthnChallenge(
                    challenge,
                    userId,
                    request.getUsername(),
                    WebAuthnChallenge.ChallengeType.AUTHENTICATION,
                    LocalDateTime.now().plusMinutes(5));
            challengeRepository.save(challengeEntity);

            // Create authentication response
            AuthenticationStartResponse response = new AuthenticationStartResponse();
            response.setChallenge(challenge);
            response.setRpId(rpId);
            response.setTimeout(timeout);
            response.setUserVerification("required"); // Require biometric verification

            List<String> credentialIds = keycloakWebAuthnService.getCredentialIds(userId);
            AuthenticationStartResponse.AllowCredentials[] allowCredentials = credentialIds.stream()
                    .map(credId -> new AuthenticationStartResponse.AllowCredentials(
                            "public-key",
                            credId,
                            new String[] { "internal" } // Platform authenticator
                    ))
                    .toArray(AuthenticationStartResponse.AllowCredentials[]::new);

            response.setAllowCredentials(allowCredentials);

            logger.info("Successfully generated authentication challenge for user: {}", request.getUsername());
            return response;

        } catch (Exception e) {
            logger.error("Failed to start authentication for user: {}", request.getUsername(), e);
            throw new RuntimeException("Failed to start authentication: " + e.getMessage());
        }
    }

    public ApiResponse<AuthenticationResponse> completeAuthentication(String challenge,
            AuthenticationCompleteRequest request) {
        logger.info("Completing WebAuthn authentication for credential: {}", request.getCredentialId());

        try {

            Optional<WebAuthnChallenge> challengeOpt = challengeRepository
                    .findByChallengeAndIsUsedFalseAndExpiresAtAfter(challenge, LocalDateTime.now());

            if (!challengeOpt.isPresent()) {
                return ApiResponse.error("Invalid or expired challenge");
            }

            WebAuthnChallenge challengeEntity = challengeOpt.get();

            String publicKey = keycloakWebAuthnService.getPublicKeyByCredentialId(challengeEntity.getUserId(),
                    request.getCredentialId());
            if (publicKey == null) {
                return ApiResponse.error("Credential not found");
            }

            challengeEntity.setIsUsed(true);
            challengeRepository.save(challengeEntity);

            byte[] credentialId = Base64UrlUtil.decode(request.getCredentialId());
            byte[] clientDataJSON = Base64UrlUtil.decode(request.getClientDataJSON());
            byte[] authenticatorData = Base64UrlUtil.decode(request.getAuthenticatorData());
            byte[] signature = Base64UrlUtil.decode(request.getSignature());

            AuthenticationRequest authRequest = new AuthenticationRequest(
                    credentialId,
                    authenticatorData,
                    clientDataJSON,
                    signature);

            AuthenticationParameters authParameters = new AuthenticationParameters(
                    new ServerProperty(new Origin(origin), rpId, new DefaultChallenge(challenge), null),
                    null,
                    null,
                    false,
                    true);

            // WebAuthn verifies the cryptographic
            // signature
            AuthenticationData authData = webAuthnManager.parse(authRequest);
            webAuthnManager.validate(authData, authParameters);

            KeycloakWebAuthnCredential credential = keycloakWebAuthnService.getWebAuthnCredential(
                    challengeEntity.getUserId(), request.getCredentialId());

            if (credential == null) {
                throw new RuntimeException("Credential not found for user");
            }

            keycloakWebAuthnService.updateSignatureCount(challengeEntity.getUserId(), request.getCredentialId(),
                    authData.getAuthenticatorData().getSignCount());

            String username = keycloakService.getUsernameById(challengeEntity.getUserId());

            String token = jwtService.generateToken(challengeEntity.getUserId(), username);

            AuthenticationResponse authResponse = new AuthenticationResponse(
                    token,
                    challengeEntity.getUserId(),
                    username,
                    jwtService.getExpirationTime());

            logger.info("Successfully authenticated user: {} using WebAuthn", username);
            return ApiResponse.success("Authentication successful", authResponse);

        } catch (Exception e) {
            logger.error("Failed to complete authentication for credential: {}", request.getCredentialId(), e);
            return ApiResponse.error("Authentication failed: " + e.getMessage());
        }
    }

    public List<KeycloakWebAuthnCredential> getUserCredentials(String userId) {
        return keycloakWebAuthnService.getUserCredentials(userId);
    }

    public boolean hasCredentials(String userId) {
        return keycloakWebAuthnService.hasWebAuthnCredentials(userId);
    }

    public boolean deleteCredential(String userId, String credentialId) {
        return keycloakWebAuthnService.deleteCredential(userId, credentialId);
    }

    @Transactional
    public void cleanupExpiredChallenges() {
        challengeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        challengeRepository.deleteByIsUsedTrue();
    }
}
