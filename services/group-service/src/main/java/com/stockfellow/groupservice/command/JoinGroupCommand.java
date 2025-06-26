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

    /**
     * Create a join request for any group (public or private)
     * This method adds the user to the group's request array
     */
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
        data.put("requestedAt", System.currentTimeMillis());

        // Append the event
        Event event = eventStoreService.appendEvent("JoinRequestCreated", data);
        
        // Rebuild the read model
        readModelService.rebuildState(groupId);
        
        logger.info("User {} created join request {} for group {}", userId, requestId, groupId);
        return event.getId();
    }

    /**
     * Direct join method (now deprecated - kept for backward compatibility)
     * This method should not be used anymore as all joins now require admin approval
     */
    @Deprecated
    public String execute(String groupId, String userId) {
        logger.warn("Direct join method called - this is deprecated. All joins now require admin approval.");
        throw new IllegalStateException("Direct joining is not allowed. All users must request to join and wait for admin approval.");
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

        // For public groups, allow request creation
        if ("Public".equals(group.getVisibility())) {
            return; // Valid request for public group
        }

        // For private groups, only allow if user has an invite (this would be handled separately)
        // For now, we'll throw an exception as private group invites are not implemented yet
        throw new IllegalStateException("Cannot request to join private groups without an invite link");
    }
}