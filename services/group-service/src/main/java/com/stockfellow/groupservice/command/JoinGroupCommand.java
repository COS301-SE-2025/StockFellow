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

        // For private groups, you might want to add additional validation
        // such as invitation-only logic
        if ("Private".equals(group.getVisibility())) {
            // Add any private group join validation logic here
            logger.info("User {} attempting to join private group {}", userId, group.getGroupId());
        }
    }
}