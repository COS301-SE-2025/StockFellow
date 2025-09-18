package com.stockfellow.keycloak;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UserSyncEventListenerProviderFactory implements EventListenerProviderFactory {

    private String userServiceUrl;
    private String transactionServiceUrl;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new UserSyncEventListener(session, userServiceUrl, transactionServiceUrl);
    }

    @Override
    public void init(Config.Scope config) {
        userServiceUrl = config.get("userServiceUrl", "http://user-service:4020");
        transactionServiceUrl = config.get("transactionServiceUrl", "http://transaction-service:4080");
        System.out.println("UserSync EventListener initialized with URLs: " + userServiceUrl + " and " + transactionServiceUrl);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Post initialization steps, none for now
    }

    @Override
    public void close() {
        // Cleanup
    }

    @Override
    public String getId() {
        return "user-sync-event-listener";
    }
}