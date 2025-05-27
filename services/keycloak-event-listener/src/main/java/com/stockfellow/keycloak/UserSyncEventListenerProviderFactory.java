package com.stockfellow.keycloak;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UserSyncEventListenerProviderFactory implements EventListenerProviderFactory {

    private String userServiceUrl;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new UserSyncEventListener(session, userServiceUrl);
    }

    @Override
    public void init(Config.Scope config) {
        userServiceUrl = config.get("userServiceUrl", "http://user-service:4000");
        System.out.println("UserSync EventListener initialized with URL: " + userServiceUrl);
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