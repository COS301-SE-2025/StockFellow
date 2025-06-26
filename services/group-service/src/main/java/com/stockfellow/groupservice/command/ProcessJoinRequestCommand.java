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

/**
 * Command to process join requests for groups.
 * 
 * This command handles the processing of join requests by group admins.
 * It supports two actions:
 * - "accept": Adds the user to the group members and removes the request
 * - "reject": Removes the request without adding the user to the group
 * 
 * Flow:
 * 1. User requests to join a public group via GET /{groupId}/join
 * 2. Request is added to the group's requests array
 * 3. Admin views requests via GET /{groupId}/requests
 * 4. Admin processes request via POST /{groupId}/request with action "accept" or "reject"
 * 5. If accepted: User is added to members array and given group access
 * 6. If rejected: Request is removed, user is not given access
 */
@Component
public class ProcessJoinRequestCommand {
    private static final Logger logger = LoggerFactory.getLogger(ProcessJoinRequestCommand.class);
    private final EventStoreService eventStoreService;
    private final ReadModelService readModelService;

    public ProcessJoinRequestCommand(EventStoreService eventStoreService, ReadModelService readModelService) {
        this.eventStoreService = eventStoreService;
        this.readModelService = readModelService;
    }

    /**
     * Processes a join request for a group.
     * 
     * @param groupId The ID of the group
     * @param requestId The ID of the join request to process
     * @param action The action to take ("accept" or "reject")
     * @param adminId The ID of the admin processing the request
     * @return The ID of the created event
     * @throws IllegalStateException if validation fails
     */
    public String execute(String groupId, String requestId, String action, String adminId) {
        logger.info("Processing join request {} for group {} with action {} by admin {}", 
                   requestId, groupId, action, adminId);

        // Validate input parameters
        validateInputParameters(groupId, requestId, action, adminId);

        // Get the current group state
        Group group = readModelService.getGroup(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found with ID: " + groupId));

        // Validate that the admin has permissions to process the request
        validateAdminPermissions(group, adminId);

        // Find the join request
        Group.JoinRequest joinRequest = findJoinRequest(group, requestId);
        if (joinRequest == null) {
            throw new IllegalStateException("Join request not found with ID: " + requestId);
        }

        // Validate the request state
        if (!"waiting".equals(joinRequest.getState())) {
            throw new IllegalStateException("Join request has already been processed. Current state: " + joinRequest.getState());
        }

        Event event;
        
        if ("accept".equals(action)) {
            event = processAcceptRequest(group, joinRequest, adminId, groupId, requestId);
        } else {
            event = processRejectRequest(joinRequest, adminId, groupId, requestId);
        }

        // Rebuild the read model to reflect the changes
        readModelService.rebuildState(groupId);
        
        logger.info("Successfully processed join request {} for group {} with action {}", 
                   requestId, groupId, action);
        
        return event.getId();
    }

    /**
     * Validates input parameters for the execute method.
     */
    private void validateInputParameters(String groupId, String requestId, String action, String adminId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty");
        }
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID cannot be null or empty");
        }
        if (!action.equals("accept") && !action.equals("reject")) {
            throw new IllegalArgumentException("Action must be 'accept' or 'reject', but was: " + action);
        }
    }

    /**
     * Processes an accept request by adding the user to the group.
     */
    private Event processAcceptRequest(Group group, Group.JoinRequest joinRequest, 
                                     String adminId, String groupId, String requestId) {
        // Validate that group can accept new members
        validateGroupCapacity(group);
        
        // Check if user is not already a member (safety check)
        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(joinRequest.getUserId()));
        
        if (alreadyMember) {
            throw new IllegalStateException("User is already a member of this group");
        }
        
        // Create event data for accepting the request
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("requestId", requestId);
        data.put("userId", joinRequest.getUserId());
        data.put("action", "accept");
        data.put("processedBy", adminId);
        data.put("processedAt", System.currentTimeMillis());
        data.put("role", "member"); // New members get member role by default
        data.put("joinedAt", System.currentTimeMillis());

        // Append the event - this will add user to group and remove request
        Event event = eventStoreService.appendEvent("JoinRequestAccepted", data);
        
        logger.info("Join request {} accepted by admin {} for group {}. User {} added to group.", 
                   requestId, adminId, groupId, joinRequest.getUserId());
        
        return event;
    }

    /**
     * Processes a reject request by removing the request without adding the user.
     */
    private Event processRejectRequest(Group.JoinRequest joinRequest, String adminId, 
                                     String groupId, String requestId) {
        // Create event data for rejecting the request
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("requestId", requestId);
        data.put("userId", joinRequest.getUserId());
        data.put("action", "reject");
        data.put("processedBy", adminId);
        data.put("processedAt", System.currentTimeMillis());
        data.put("reason", "Request rejected by admin"); // Optional reason field

        // Append the event - this will remove the request from group
        Event event = eventStoreService.appendEvent("JoinRequestRejected", data);
        
        logger.info("Join request {} rejected by admin {} for group {}. User {} denied access.", 
                   requestId, adminId, groupId, joinRequest.getUserId());
        
        return event;
    }

    /**
     * Validates that the admin has permissions to process join requests.
     * 
     * Permissions are granted to:
     * 1. The group creator (adminId matches group.getAdminId())
     * 2. Members with "admin" or "founder" roles
     */
    private void validateAdminPermissions(Group group, String adminId) {
        // Check if user is the group admin (creator)
        if (group.getAdminId().equals(adminId)) {
            logger.debug("Admin permission granted: User {} is the group creator", adminId);
            return;
        }

        // Check if user is a member with admin or founder role
        boolean isAuthorizedMember = group.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(adminId) && 
                         ("admin".equals(member.getRole()) || "founder".equals(member.getRole())));

        if (isAuthorizedMember) {
            logger.debug("Admin permission granted: User {} has admin/founder role", adminId);
            return;
        }

        logger.warn("Permission denied: User {} attempted to process join request without proper permissions", adminId);
        throw new IllegalStateException("User does not have permission to process join requests for this group");
    }

    /**
     * Finds a join request in the group by request ID.
     * 
     * @param group The group to search in
     * @param requestId The request ID to find
     * @return The join request if found, null otherwise
     */
    private Group.JoinRequest findJoinRequest(Group group, String requestId) {
        return group.getRequests().stream()
                .filter(request -> request.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates that the group has capacity for new members.
     * 
     * @param group The group to check
     * @throws IllegalStateException if the group is at maximum capacity
     */
    private void validateGroupCapacity(Group group) {
        int currentMembers = group.getMembers().size();
        int maxMembers = group.getMaxMembers();
        
        if (currentMembers >= maxMembers) {
            logger.warn("Group capacity validation failed: Current members ({}) >= Max members ({})", 
                       currentMembers, maxMembers);
            throw new IllegalStateException(
                String.format("Cannot accept join request: group is at maximum capacity (%d/%d members)", 
                            currentMembers, maxMembers));
        }
        
        logger.debug("Group capacity validation passed: {}/{} members", currentMembers, maxMembers);
    }
}