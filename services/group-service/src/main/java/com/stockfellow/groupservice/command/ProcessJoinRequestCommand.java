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
import java.util.Optional;

@Component
public class ProcessJoinRequestCommand {
    private static final Logger logger = LoggerFactory.getLogger(ProcessJoinRequestCommand.class);
    private final EventStoreService eventStoreService;
    private final ReadModelService readModelService;

    public ProcessJoinRequestCommand(EventStoreService eventStoreService, ReadModelService readModelService) {
        this.eventStoreService = eventStoreService;
        this.readModelService = readModelService;
    }

    public String execute(String groupId, String requestId, String action, String adminId) {
        // Get the current group state
        Group group = readModelService.getGroup(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        // Validate that the admin has permissions to process the request
        validateAdminPermissions(group, adminId);

        // Find the join request
        Group.JoinRequest joinRequest = findJoinRequest(group, requestId);
        if (joinRequest == null) {
            throw new IllegalStateException("Join request not found");
        }

        // Validate the request state
        if (!"waiting".equals(joinRequest.getState())) {
            throw new IllegalStateException("Join request has already been processed");
        }

        // Validate the action
        if (!action.equals("accept") && !action.equals("reject")) {
            throw new IllegalStateException("Action must be 'accept' or 'reject'");
        }

        Event event;
        
        if ("accept".equals(action)) {
            // Validate that group can accept new members
            validateGroupCapacity(group);
            
            // Create event data for accepting the request
            Map<String, Object> data = new HashMap<>();
            data.put("groupId", groupId);
            data.put("requestId", requestId);
            data.put("userId", joinRequest.getUserId());
            data.put("action", "accept");
            data.put("processedBy", adminId);
            data.put("role", "member"); // New members get member role by default

            // Append the event - this will add user to group and remove request
            event = eventStoreService.appendEvent("JoinRequestAccepted", data);
            
            logger.info("Join request {} accepted by admin {} for group {}", requestId, adminId, groupId);
            
        } else { // reject
            // Create event data for rejecting the request
            Map<String, Object> data = new HashMap<>();
            data.put("groupId", groupId);
            data.put("requestId", requestId);
            data.put("userId", joinRequest.getUserId());
            data.put("action", "reject");
            data.put("processedBy", adminId);

            // Append the event - this will remove the request from group
            event = eventStoreService.appendEvent("JoinRequestRejected", data);
            
            logger.info("Join request {} rejected by admin {} for group {}", requestId, adminId, groupId);
        }

        // Rebuild the read model to reflect the changes
        readModelService.rebuildState(groupId);
        
        return event.getId();
    }

    private void validateAdminPermissions(Group group, String adminId) {
        // Check if user is the group admin (creator)
        if (group.getAdminId().equals(adminId)) {
            return;
        }

        // Check if user is a member with admin or founder role
        boolean isAuthorizedMember = group.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(adminId) && 
                         ("admin".equals(member.getRole()) || "founder".equals(member.getRole())));

        if (!isAuthorizedMember) {
            throw new IllegalStateException("User does not have permission to process join requests for this group");
        }
    }

    private Group.JoinRequest findJoinRequest(Group group, String requestId) {
        return group.getRequests().stream()
                .filter(request -> request.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);
    }

    private void validateGroupCapacity(Group group) {
        if (group.getMembers().size() >= group.getMaxMembers()) {
            throw new IllegalStateException("Cannot accept join request: group is at maximum capacity");
        }
    }
}
