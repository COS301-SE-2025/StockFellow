package com.stockfellow.notificationservice.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Component
public class GroupServiceClient {
    private final RestTemplate restTemplate;
    private final String groupServiceUrl = "http://localhost:4040/api/groups";

    public GroupServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean groupExists(String groupId) {
        try {
            restTemplate.getForEntity(groupServiceUrl + "/{groupId}", Object.class, groupId);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    public List<String> getGroupMemberIds(String groupId) {
        // Mock implementation; replace with actual API call
        return restTemplate.getForObject(groupServiceUrl + "/{groupId}/members", List.class, groupId);
    }

    public String getGroupName(String groupId) {
        // Mock implementation; replace with actual API call
        Map<String, Object> group = restTemplate.getForObject(groupServiceUrl + "/{groupId}", Map.class, groupId);
        return group != null ? (String) group.get("name") : "Unknown Group";
    }
}
