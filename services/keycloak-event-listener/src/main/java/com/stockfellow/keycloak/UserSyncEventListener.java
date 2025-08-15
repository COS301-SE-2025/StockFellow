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
    private final String transactionServiceUrl;
    
    public UserSyncEventListener(KeycloakSession session, String userServiceUrl, String transactionServiceUrl) {
        this.session = session;
        this.userServiceUrl = userServiceUrl;
        this.transactionServiceUrl = transactionServiceUrl;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.REGISTER || event.getType() == EventType.UPDATE_PROFILE) {
            syncUser(event.getUserId());
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if (event.getOperationType().name().equals("CREATE") && 
            event.getResourceType().name().equals("USER")) {
            String resourcePath = event.getResourcePath();
            if (resourcePath != null && resourcePath.startsWith("users/")) {
                String userId = resourcePath.substring(6);
                syncUser(userId);
            }
        }
    }

    private void syncUser(String userId) {
        try {
            RealmModel realm = session.getContext().getRealm();
            UserModel user = session.users().getUserById(realm, userId);
            
            if (user != null) {
                // Sync to User Service
                syncToUserService(user);
                
                // Sync to Transaction Service
                syncToTransactionService(user);
                
                System.out.println("Successfully synced user to both services: " + userId);
            }
        } catch (Exception e) {
            System.err.println("Failed to sync user " + userId + ": " + e.getMessage());
        }
    }
    
    private void syncToUserService(UserModel user) {
        try {
            String payload = createUserServicePayload(user);
            sendRequest(userServiceUrl + "/sync", payload, "User Service");
        } catch (Exception e) {
            System.err.println("Failed to sync to User Service: " + e.getMessage());
        }
    }
    
    private void syncToTransactionService(UserModel user) {
        try {
            String payload = createTransactionServicePayload(user);
            sendRequest(transactionServiceUrl + "/api/users/sync", payload, "Transaction Service");
        } catch (Exception e) {
            System.err.println("Failed to sync to Transaction Service: " + e.getMessage());
        }
    }
    
    private void sendRequest(String url, String payload, String serviceName) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("Successfully synced to " + serviceName);
            } else {
                System.err.println("Failed to sync to " + serviceName + 
                                 ". Status: " + response.statusCode() + 
                                 ", Body: " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("Error calling " + serviceName + ": " + e.getMessage());
        }
    }
    
    private String createUserServicePayload(UserModel user) {
        Map<String, List<String>> attributes = user.getAttributes();
        String contactNumber = getFirstAttribute(attributes, "contactNumber");
        String idNumber = getFirstAttribute(attributes, "idNumber");
        
        return String.format("""
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
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName() != null ? user.getFirstName() : "",
            user.getLastName() != null ? user.getLastName() : "",
            user.isEmailVerified(),
            contactNumber != null ? contactNumber : "",
            idNumber != null ? idNumber : ""
        );
    }
    
    private String createTransactionServicePayload(UserModel user) {
        Map<String, List<String>> attributes = user.getAttributes();
        String contactNumber = getFirstAttribute(attributes, "contactNumber");
        return String.format("""
            {
                "userId": "%s",
                "email": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "phone": "%s",
                "status": "active"
            }""",
            user.getId(),
            user.getEmail(),
            user.getFirstName() != null ? user.getFirstName() : "",
            user.getLastName() != null ? user.getLastName() : "",
            contactNumber != null ? contactNumber : ""
        );
    }
    
    private String getFirstAttribute(Map<String, List<String>> attributes, String key) {
        List<String> values = attributes.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    @Override
    public void close() {
        // Cleanup if needed
    }
}