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
        Group group = readModelService.getGroup(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found"));
        if (group.getMemberIds().contains(userId)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("userId", userId);

        Event event = eventStoreService.appendEvent("MemberAdded", data);
        readModelService.rebuildState(groupId);
        logger.info("User {} joined group {}", userId, groupId);

        return event.getId();
    }
}

