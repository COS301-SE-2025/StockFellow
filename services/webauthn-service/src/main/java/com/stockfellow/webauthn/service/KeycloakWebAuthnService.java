package com.stockfellow.webauthn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.webauthn.entity.KeycloakWebAuthnCredential;
import com.stockfellow.webauthn.repository.KeycloakWebAuthnCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

// manage WebAuthn credentials stored in Keycloak's credential table
@Service
@Transactional
public class KeycloakWebAuthnService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakWebAuthnService.class);

    private final KeycloakWebAuthnCredentialRepository credentialRepository;
    private final ObjectMapper objectMapper;

    public KeycloakWebAuthnService(KeycloakWebAuthnCredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void storeWebAuthnCredential(String userId, String credentialId, String publicKey,
            String authenticatorName, long signatureCount) {
        try {

            Map<String, Object> credentialData = new HashMap<>();
            credentialData.put("credentialId", credentialId);
            credentialData.put("publicKey", publicKey);
            credentialData.put("signatureCount", signatureCount);
            credentialData.put("credentialType", "public-key");

            Map<String, Object> secretData = new HashMap<>();
            secretData.put("authenticatorName", authenticatorName);
            secretData.put("registrationTime", System.currentTimeMillis());

            KeycloakWebAuthnCredential credential = new KeycloakWebAuthnCredential();
            credential.setId(UUID.randomUUID().toString());
            credential.setUserId(userId);
            credential.setUserLabel(authenticatorName);
            credential.setCredentialData(objectMapper.writeValueAsString(credentialData));
            credential.setSecretData(objectMapper.writeValueAsString(secretData));

            credentialRepository.save(credential);

            logger.info("Stored WebAuthn credential for user: {} with device: {}", userId, authenticatorName);

        } catch (Exception e) {
            logger.error("Failed to store WebAuthn credential for user: {}", userId, e);
            throw new RuntimeException("Failed to store WebAuthn credential", e);
        }
    }

    public boolean hasWebAuthnCredentials(String userId) {
        return credentialRepository.existsByUserIdAndType(userId);
    }

    public KeycloakWebAuthnCredential getWebAuthnCredential(String userId, String credentialId) {
        try {
            List<KeycloakWebAuthnCredential> credentials = credentialRepository.findByUserIdAndType(userId);

            for (KeycloakWebAuthnCredential credential : credentials) {
                @SuppressWarnings("unchecked")
                Map<String, Object> credentialData = objectMapper.readValue(
                        credential.getCredentialData(), Map.class);

                if (credentialId.equals(credentialData.get("credentialId"))) {
                    return credential;
                }
            }

            return null;
        } catch (Exception e) {
            logger.error("Failed to get WebAuthn credential for user: {} and credentialId: {}", userId, credentialId,
                    e);
            return null;
        }
    }

    public List<String> getCredentialIds(String userId) {
        try {
            List<KeycloakWebAuthnCredential> credentials = credentialRepository.findByUserIdAndType(userId);
            List<String> credentialIds = new ArrayList<>();

            for (KeycloakWebAuthnCredential credential : credentials) {
                @SuppressWarnings("unchecked")
                Map<String, Object> credentialData = objectMapper.readValue(
                        credential.getCredentialData(), Map.class);
                String credentialId = (String) credentialData.get("credentialId");
                if (credentialId != null) {
                    credentialIds.add(credentialId);
                }
            }

            return credentialIds;
        } catch (Exception e) {
            logger.error("Failed to get credential IDs for userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public void updateSignatureCount(String userId, String credentialId, long signatureCount) {
        try {
            List<KeycloakWebAuthnCredential> credentials = credentialRepository.findByUserIdAndType(userId);

            for (KeycloakWebAuthnCredential credential : credentials) {
                @SuppressWarnings("unchecked")
                Map<String, Object> credentialData = objectMapper.readValue(
                        credential.getCredentialData(), Map.class);

                if (credentialId.equals(credentialData.get("credentialId"))) {
                    credentialData.put("signatureCount", signatureCount);
                    credentialData.put("lastUsed", System.currentTimeMillis());

                    credential.setCredentialData(objectMapper.writeValueAsString(credentialData));
                    credentialRepository.save(credential);

                    logger.debug("Updated signature count for credential: {}", credentialId);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to update signature count for credentialId: {}", credentialId, e);
        }
    }

    public boolean removeCredential(String userId, String deviceName) {
        try {
            var credentialOpt = credentialRepository.findByUserIdAndUserLabel(userId, deviceName);
            if (credentialOpt.isPresent()) {
                credentialRepository.delete(credentialOpt.get());
                logger.info("Removed WebAuthn credential for user: {} device: {}", userId, deviceName);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to remove credential for user: {} device: {}", userId, deviceName, e);
            return false;
        }
    }

    public String getPublicKeyByCredentialId(String userId, String credentialId) {
        try {
            List<KeycloakWebAuthnCredential> credentials = credentialRepository.findByUserIdAndType(userId);

            for (KeycloakWebAuthnCredential credential : credentials) {
                @SuppressWarnings("unchecked")
                Map<String, Object> credentialData = objectMapper.readValue(
                        credential.getCredentialData(), Map.class);

                if (credentialId.equals(credentialData.get("credentialId"))) {
                    return (String) credentialData.get("publicKey");
                }
            }

            return null;
        } catch (Exception e) {
            logger.error("Failed to get public key for credentialId: {}", credentialId, e);
            return null;
        }
    }

    public List<KeycloakWebAuthnCredential> getUserCredentials(String userId) {
        return credentialRepository.findByUserIdAndType(userId);
    }

    public boolean deleteCredential(String userId, String credentialId) {
        try {
            List<KeycloakWebAuthnCredential> credentials = credentialRepository.findByUserIdAndType(userId);

            for (KeycloakWebAuthnCredential credential : credentials) {
                @SuppressWarnings("unchecked")
                Map<String, Object> credentialData = objectMapper.readValue(
                        credential.getCredentialData(), Map.class);

                if (credentialId.equals(credentialData.get("credentialId"))) {
                    credentialRepository.delete(credential);
                    logger.info("Deleted credential {} for user {}", credentialId, userId);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            logger.error("Failed to delete credential {} for user {}", credentialId, userId, e);
            return false;
        }
    }

}
