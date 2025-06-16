package com.stockfellow.groupservice.command;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.service.ReadModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CreateGroupCommand {
    private static final Logger logger = LoggerFactory.getLogger(CreateGroupCommand.class);
    private final EventStoreService eventStoreService;
    private final ReadModelService readModelService;

    public CreateGroupCommand(EventStoreService eventStoreService, ReadModelService readModelService) {
        this.eventStoreService = eventStoreService;
        this.readModelService = readModelService;
    }

    public String execute(String groupId, String adminId, String name, Double minContribution,
                         Integer maxMembers, String description, String profileImage,
                         String visibility, String contributionFrequency, Date contributionDate,
                         String payoutFrequency, Date payoutDate, List<String> memberIds) {
        // Validate inputs
        if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(contributionFrequency)) {
            throw new IllegalArgumentException("Invalid contribution frequency");
        }
        if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(payoutFrequency)) {
            throw new IllegalArgumentException("Invalid payout frequency");
        }
        if (!Arrays.asList("Private", "Public").contains(visibility)) {
            throw new IllegalArgumentException("Invalid visibility");
        }
        if (minContribution == null || minContribution <= 0) {
            throw new IllegalArgumentException("Invalid minimum contribution");
        }
        if (maxMembers == null || maxMembers <= 0) {
            throw new IllegalArgumentException("Invalid maximum number of members");
        }
        if (memberIds != null && memberIds.size() > maxMembers) {
            throw new IllegalArgumentException("Number of memberIds cannot exceed maxMembers");
        }
        if (contributionDate != null && contributionDate.toString().isEmpty()) {
            throw new IllegalArgumentException("Invalid contribution date");
        }
        if (payoutDate != null && payoutDate.toString().isEmpty()) {
            throw new IllegalArgumentException("Invalid payout date");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("adminId", adminId);
        data.put("name", name);
        data.put("minContribution", minContribution);
        data.put("maxMembers", maxMembers);
        data.put("description", description);
        data.put("profileImage", profileImage != null ? profileImage : null);
        data.put("visibility", visibility);
        data.put("contributionFrequency", contributionFrequency);
        data.put("contributionDate", contributionDate != null ? contributionDate.toString() : null);
        data.put("payoutFrequency", payoutFrequency);
        data.put("payoutDate", payoutDate != null ? payoutDate.toString() : null);
        data.put("memberIds", memberIds != null ? memberIds : new ArrayList<>());

        Event event = eventStoreService.appendEvent("GroupCreated", data);
        readModelService.rebuildState(groupId);
        logger.info("Group {} created by admin {}", groupId, adminId);

        return event.getId();
    }
}
