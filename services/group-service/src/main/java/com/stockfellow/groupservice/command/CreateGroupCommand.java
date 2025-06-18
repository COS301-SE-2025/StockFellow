package com.stockfellow.groupservice.command;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
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
        validateInputs(name, minContribution, maxMembers, visibility, contributionFrequency, payoutFrequency, memberIds);

        // Create the members list with proper roles and initial contributions
        List<Group.Member> members = new ArrayList<>();
        
        if (memberIds != null && !memberIds.isEmpty()) {
            for (String memberId : memberIds) {
                Group.Member member;
                if (memberId.equals(adminId)) {
                    // Admin gets founder role and initial contribution set to minContribution
                    member = new Group.Member(memberId, "founder");
                    member.setContribution(minContribution); // Admin starts with minimum contribution
                } else {
                    // Other members get member role and zero initial contribution
                    member = new Group.Member(memberId, "member");
                    member.setContribution(0.0); // Members start with no contribution
                }
                members.add(member);
            }
        } else {
            // If no memberIds provided, just add the admin
            Group.Member adminMember = new Group.Member(adminId, "founder");
            adminMember.setContribution(minContribution); // Admin starts with minimum contribution
            members.add(adminMember);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("name", name);
        data.put("adminId", adminId);
        data.put("minContribution", minContribution);
        data.put("balance", minContribution); // Initialize balance with admin's initial contribution
        data.put("maxMembers", maxMembers);
        data.put("description", description);
        data.put("profileImage", profileImage);
        data.put("visibility", visibility);
        data.put("contributionFrequency", contributionFrequency);
        data.put("contributionDate", contributionDate != null ? contributionDate.toString() : null);
        data.put("payoutFrequency", payoutFrequency);
        data.put("payoutDate", payoutDate != null ? payoutDate.toString() : null);
        data.put("createdAt", new Date());
        data.put("members", convertMembersToMap(members));

        Event event = eventStoreService.appendEvent("GroupCreated", data);
        readModelService.rebuildState(groupId);
        
        logger.info("Group {} created by admin {} with {} initial members", groupId, adminId, members.size());

        return event.getId();
    }

    private void validateInputs(String name, Double minContribution, Integer maxMembers, 
                               String visibility, String contributionFrequency, String payoutFrequency, 
                               List<String> memberIds) {
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        
        if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(contributionFrequency)) {
            throw new IllegalArgumentException("Invalid contribution frequency. Must be: Monthly, Bi-weekly, or Weekly");
        }
        
        if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(payoutFrequency)) {
            throw new IllegalArgumentException("Invalid payout frequency. Must be: Monthly, Bi-weekly, or Weekly");
        }
        
        if (!Arrays.asList("Private", "Public").contains(visibility)) {
            throw new IllegalArgumentException("Invalid visibility. Must be: Private or Public");
        }
        
        if (minContribution == null || minContribution <= 0) {
            throw new IllegalArgumentException("Minimum contribution must be greater than 0");
        }
        
        if (maxMembers == null || maxMembers <= 0) {
            throw new IllegalArgumentException("Maximum members must be greater than 0");
        }
        
        if (memberIds != null && memberIds.size() >= maxMembers) {
            throw new IllegalArgumentException("Number of initial members cannot exceed maximum members (admin takes one slot)");
        }
    }

    private List<Map<String, Object>> convertMembersToMap(List<Group.Member> members) {
        List<Map<String, Object>> memberMaps = new ArrayList<>();
        for (Group.Member member : members) {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("userId", member.getUserId());
            memberMap.put("role", member.getRole());
            memberMap.put("joinedAt", member.getJoinedAt());
            memberMap.put("contribution", member.getContribution());
            memberMap.put("lastActive", member.getLastActive());
            memberMaps.add(memberMap);
        }
        return memberMaps;
    }
}