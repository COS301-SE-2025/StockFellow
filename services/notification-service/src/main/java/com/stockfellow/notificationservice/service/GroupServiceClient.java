package com.stockfellow.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class GroupServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(GroupServiceClient.class);
    
    private final WebClient webClient;
    
    @Value("${services.group-service.url}")
    private String groupServiceUrl;
    
    public GroupServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Map<String, Object> getGroupDetails(String groupId) {
        try {
            return webClient.get()
                    .uri(groupServiceUrl + "/api/groups/{groupId}/view", groupId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching group details for groupId {}: {}", groupId, e.getMessage());
            return null;
        }
    }
    
    public String getGroupName(String groupId) {
        Map<String, Object> groupDetails = getGroupDetails(groupId);
        if (groupDetails != null && groupDetails.containsKey("group")) {
            Map<String, Object> group = (Map<String, Object>) groupDetails.get("group");
            return (String) group.get("name");
        }
        return "Unknown Group";
    }
    
    public boolean isUserGroupMember(String userId, String groupId) {
        try {
            Map<String, Object> groupDetails = getGroupDetails(groupId);
            if (groupDetails != null && groupDetails.containsKey("userPermissions")) {
                Map<String, Object> permissions = (Map<String, Object>) groupDetails.get("userPermissions");
                return Boolean.TRUE.equals(permissions.get("isMember"));
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking group membership for user {} in group {}: {}", userId, groupId, e.getMessage());
            return false;
        }
    }
}