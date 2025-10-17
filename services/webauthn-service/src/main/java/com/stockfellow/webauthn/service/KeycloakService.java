package com.stockfellow.webauthn.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

// interact with Keycloak to verify users and get user information
@Service
public class KeycloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void initKeycloak() {
        try {
            this.keycloak = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm("master") // Admin connection uses master realm
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId("admin-cli")
                    .build();

            this.realmResource = keycloak.realm(realm);
            logger.info("Keycloak service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Keycloak service", e);
            throw new RuntimeException("Failed to initialize Keycloak service", e);
        }
    }

    public boolean userExists(String userId) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            return user != null;
        } catch (Exception e) {
            logger.debug("User not found in Keycloak: {}", userId);
            return false;
        }
    }

    public UserRepresentation getUserByUsername(String username) {
        try {
            List<UserRepresentation> users = realmResource.users().search(username, true);
            if (!users.isEmpty()) {
                return users.get(0);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error fetching user by username: {}", username, e);
            return null;
        }
    }

    public UserRepresentation getUserById(String userId) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            return userResource.toRepresentation();
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", userId, e);
            return null;
        }
    }

    public String getUserId(String username) {
        UserRepresentation user = getUserByUsername(username);
        return user != null ? user.getId() : null;
    }

    public String getUserIdByUsername(String username) {
        return getUserId(username);
    }

    public String getUsername(String userId) {
        UserRepresentation user = getUserById(userId);
        return user != null ? user.getUsername() : null;
    }

    public String getUsernameById(String userId) {
        UserRepresentation user = getUserById(userId);
        return user != null ? user.getUsername() : null;
    }
}
