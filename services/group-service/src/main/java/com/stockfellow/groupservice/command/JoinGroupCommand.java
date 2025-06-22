package com.stockfellow.groupservice.command;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.service.ReadModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JoinGroupCommand {
    private static final Logger logger = LoggerFactory.getLogger(JoinGroupCommand.class);
    private final EventStoreService eventStoreService;
    private final ReadModelService readModelService;

    public JoinGroupCommand(EventStoreService eventStoreService, ReadModelService readModelService) {
        this.eventStoreService = eventStoreService;
        this.readModelService = readModelService;
    }

    public String execute(String groupId, String userId) {
        // Get the current group state
        Group group = readModelService.getGroup(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        // Validate that the group exists and user can join
        validateJoinRequest(group, userId);

        // Create the event data
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("userId", userId);
        data.put("role", "member"); // New members get member role by default

        // Append the event
        Event event = eventStoreService.appendEvent("UserJoinedGroup", data);
        
        // Rebuild the read model
        readModelService.rebuildState(groupId);
        
        logger.info("User {} joined group {}", userId, groupId);
        return event.getId();
    }

    public String createJoinRequest(String groupId, String userId) {
        // Get the current group state
        Group group = readModelService.getGroup(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        // Validate that the group exists and user can request to join
        validateJoinRequestCreation(group, userId);

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 12);

        // Create the event data
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("userId", userId);
        data.put("requestId", requestId);
        data.put("state", "waiting");

        // Append the event
        Event event = eventStoreService.appendEvent("JoinRequestCreated", data);
        
        // Rebuild the read model
        readModelService.rebuildState(groupId);
        
        logger.info("User {} created join request for group {}", userId, groupId);
        return event.getId();
    }

    private void validateJoinRequest(Group group, String userId) {
        // Check if user is already a member
        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));
        
        if (alreadyMember) {
            throw new IllegalStateException("User is already a member of this group");
        }

        // Check if group is full
        if (group.getMembers().size() >= group.getMaxMembers()) {
            throw new IllegalStateException("Group is full");
        }

        // For public groups, users can join directly
        if (!"Public".equals(group.getVisibility())) {
            throw new IllegalStateException("Cannot directly join private group without approval");
        }
    }

    private void validateJoinRequestCreation(Group group, String userId) {
        // Check if user is already a member
        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));
        
        if (alreadyMember) {
            throw new IllegalStateException("User is already a member of this group");
        }

        // Check if user already has a pending request
        boolean hasPendingRequest = group.getRequests().stream()
                .anyMatch(request -> request.getUserId().equals(userId) && "waiting".equals(request.getState()));
        
        if (hasPendingRequest) {
            throw new IllegalStateException("User already has a pending request for this group");
        }

        // Check if group is full
        if (group.getMembers().size() >= group.getMaxMembers()) {
            throw new IllegalStateException("Group is full");
        }

        // For private groups, users need to request to join
        if ("Public".equals(group.getVisibility())) {
            throw new IllegalStateException("Public groups allow direct joining, use execute() method instead");
        }
    }
}