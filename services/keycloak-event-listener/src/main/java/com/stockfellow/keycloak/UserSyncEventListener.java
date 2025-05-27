package com.stockfellow.keycloak;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Map;
import java.util.List;

public class UserSyncEventListener implements EventListenerProvider {
    
    private final KeycloakSession session;
    private final String userServiceUrl;
    
    public UserSyncEventListener(KeycloakSession session, String userServiceUrl) {
        this.session = session;
        this.userServiceUrl = userServiceUrl;
    }

    @Override
    public void onEvent(Event event) {
        // Registration
        if (event.getType() == EventType.REGISTER) {
            syncUserRegistration(event);
        }
        
        // Profile updates
        if (event.getType() == EventType.UPDATE_PROFILE) {
            syncUserUpdate(event);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Admin created users (from console)
        if (event.getOperationType().name().equals("CREATE") && 
            event.getResourceType().name().equals("USER")) {
            syncAdminCreatedUser(event);
        }
    }

    private void syncUserRegistration(Event event) {
        try {
            RealmModel realm = session.getContext().getRealm();
            UserModel user = session.users().getUserById(realm, event.getUserId());
            
            if (user != null) {
                UserSyncData userData = new UserSyncData();
                userData.keycloakId = user.getId();
                userData.username = user.getUsername();
                userData.email = user.getEmail();
                userData.firstName = user.getFirstName();
                userData.lastName = user.getLastName();
                userData.emailVerified = user.isEmailVerified();
                
                Map<String, List<String>> attributes = user.getAttributes();
                userData.contactNumber = getFirstAttribute(attributes, "contactNumber");
                userData.idNumber = getFirstAttribute(attributes, "idNumber");
                
                sendToUserService(userData);
            }
        } catch (Exception e) {
            System.err.println("Failed to sync user registration: " + e.getMessage());
        }
    }
    
    private void syncUserUpdate(Event event) {
        try {
            RealmModel realm = session.getContext().getRealm();
            UserModel user = session.users().getUserById(realm, event.getUserId());
            
            if (user != null) {
                UserSyncData userData = new UserSyncData();
                userData.keycloakId = user.getId();
                userData.username = user.getUsername();
                userData.email = user.getEmail();
                userData.firstName = user.getFirstName();
                userData.lastName = user.getLastName();
                userData.emailVerified = user.isEmailVerified();
                
                Map<String, List<String>> attributes = user.getAttributes();
                userData.contactNumber = getFirstAttribute(attributes, "contactNumber");
                userData.idNumber = getFirstAttribute(attributes, "idNumber");
                
                sendToUserService(userData);
            }
        } catch (Exception e) {
            System.err.println("Failed to sync user update: " + e.getMessage());
        }
    }
    
    private void syncAdminCreatedUser(AdminEvent event) {
        try {
            String resourcePath = event.getResourcePath();
            if (resourcePath != null && resourcePath.startsWith("users/")) {
                String userId = resourcePath.substring(6);
                
                RealmModel realm = session.getContext().getRealm();
                UserModel user = session.users().getUserById(realm, userId);
                
                if (user != null) {
                    UserSyncData userData = new UserSyncData();
                    userData.keycloakId = user.getId();
                    userData.username = user.getUsername();
                    userData.email = user.getEmail();
                    userData.firstName = user.getFirstName();
                    userData.lastName = user.getLastName();
                    userData.emailVerified = user.isEmailVerified();
                    
                    Map<String, List<String>> attributes = user.getAttributes();
                    userData.contactNumber = getFirstAttribute(attributes, "contactNumber");
                    userData.idNumber = getFirstAttribute(attributes, "idNumber");
                    
                    sendToUserService(userData);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to sync admin created user: " + e.getMessage());
        }
    }
    
    private void sendToUserService(UserSyncData userData) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String jsonBody = String.format("""
                {
                    "keycloakId": "%s",
                    "username": "%s",
                    "email": "%s",
                    "firstName": "%s",
                    "lastName": "%s",
                    "emailVerified": %s,
                    "contactNumber": "%s",
                    "idNumber": "%s"
                }""",
                userData.keycloakId,
                userData.username,
                userData.email,
                userData.firstName != null ? userData.firstName : "",
                userData.lastName != null ? userData.lastName : "",
                userData.emailVerified,
                userData.contactNumber != null ? userData.contactNumber : "",
                userData.idNumber != null ? userData.idNumber : ""
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userServiceUrl + "/sync"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getServiceToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("Successfully synced user: " + userData.keycloakId);
            } else {
                System.err.println("Failed to sync user. Status: " + response.statusCode() + 
                                 ", Body: " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("Error calling user service: " + e.getMessage());
        }
    }
    
    private String getFirstAttribute(Map<String, List<String>> attributes, String key) {
        List<String> values = attributes.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
    
    private String getServiceToken() {
        //JWT token generation or API key but empty now for testing phase
        return "";
    }

    @Override
    public void close() {
        // Cleanup
    }
    
    private static class UserSyncData {
        String keycloakId;
        String username;
        String email;
        String firstName;
        String lastName;
        boolean emailVerified;
        String contactNumber;
        String idNumber;
    }
}