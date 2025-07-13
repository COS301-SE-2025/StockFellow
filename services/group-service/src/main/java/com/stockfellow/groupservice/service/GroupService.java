package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.repository.GroupRepository;
import com.stockfellow.groupservice.dto.CreateGroupRequest;
import com.stockfellow.groupservice.dto.CreateGroupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GroupService {
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);
    
    private final EventStoreService eventStoreService;
    private final GroupRepository groupRepository;

    public GroupService(EventStoreService eventStoreService, GroupRepository groupRepository) {
        this.eventStoreService = eventStoreService;
        this.groupRepository = groupRepository;
    }

    public CreateGroupResult createGroup(CreateGroupRequest request) {
        logger.info("Creating group '{}' for adminId: {}", request.getName(), request.getAdminId());

        // Validate the request
        validateCreateGroupRequest(request);

        // Generate unique group ID
        String groupId = generateGroupId();

        // Ensure admin is included in members list
        List<String> members = new ArrayList<>(request.getMembers());
        if (!members.contains(request.getAdminId())) {
            members.add(0, request.getAdminId()); // Add admin at the beginning
        }

        // Create and publish group created event
        Map<String, Object> eventData = buildGroupCreatedEventData(groupId, request, members);
        Event event = new Event("GroupCreated", eventData);
        String eventId = eventStoreService.saveEvent(groupId, event);

        // Build and save the group read model
        Group group = buildGroupFromRequest(groupId, request, members);
        groupRepository.save(group);

        logger.info("Group created successfully with ID: {} by admin: {}", groupId, request.getAdminId());

        return new CreateGroupResult(groupId, eventId, "Group created successfully");
    }

    private void validateCreateGroupRequest(CreateGroupRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }
        if (request.getMinContribution() == null || request.getMinContribution() <= 0) {
            throw new IllegalArgumentException("Minimum contribution must be greater than 0");
        }
        if (request.getMaxMembers() == null || request.getMaxMembers() <= 0) {
            throw new IllegalArgumentException("Maximum members must be greater than 0");
        }
        if (request.getVisibility() == null || 
            !Arrays.asList("Private", "Public").contains(request.getVisibility())) {
            throw new IllegalArgumentException("Visibility must be 'Private' or 'Public'");
        }
        if (request.getContributionFrequency() == null || 
            !Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(request.getContributionFrequency())) {
            throw new IllegalArgumentException("Invalid contribution frequency. Must be: Monthly, Bi-weekly, or Weekly");
        }
        if (request.getPayoutFrequency() == null || 
            !Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(request.getPayoutFrequency())) {
            throw new IllegalArgumentException("Invalid payout frequency. Must be: Monthly, Bi-weekly, or Weekly");
        }
        if (request.getMembers().size() > request.getMaxMembers()) {
            throw new IllegalArgumentException("Number of initial members cannot exceed maximum members");
        }
    }

    private String generateGroupId() {
        return "group_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Map<String, Object> buildGroupCreatedEventData(String groupId, CreateGroupRequest request, List<String> members) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("adminId", request.getAdminId());
        eventData.put("name", request.getName());
        eventData.put("minContribution", request.getMinContribution());
        eventData.put("maxMembers", request.getMaxMembers());
        eventData.put("description", request.getDescription());
        eventData.put("profileImage", request.getProfileImage());
        eventData.put("visibility", request.getVisibility());
        eventData.put("contributionFrequency", request.getContributionFrequency());
        eventData.put("contributionDate", request.getContributionDate());
        eventData.put("payoutFrequency", request.getPayoutFrequency());
        eventData.put("payoutDate", request.getPayoutDate());
        eventData.put("members", members);
        eventData.put("createdAt", new Date());
        return eventData;
    }

    private Group buildGroupFromRequest(String groupId, CreateGroupRequest request, List<String> memberIds) {
        Group group = new Group(groupId);
        group.setName(request.getName());
        group.setAdminId(request.getAdminId());
        group.setMinContribution(request.getMinContribution());
        group.setMaxMembers(request.getMaxMembers());
        group.setDescription(request.getDescription());
        group.setProfileImage(request.getProfileImage());
        group.setVisibility(request.getVisibility());
        group.setContributionFrequency(request.getContributionFrequency());
        group.setContributionDate(request.getContributionDate());
        group.setPayoutFrequency(request.getPayoutFrequency());
        group.setPayoutDate(request.getPayoutDate());
        group.setBalance(0.0);
        group.setCreatedAt(new Date());

        // Add members
        List<Group.Member> members = new ArrayList<>();
        for (String memberId : memberIds) {
            String role = memberId.equals(request.getAdminId()) ? "founder" : "member";
            members.add(new Group.Member(memberId, role));
        }
        group.setMembers(members);
        group.setRequests(new ArrayList<>());

        return group;
    }

    public Optional<Group> getGroup(String groupId) {
        return groupRepository.findByGroupId(groupId);
    }

    public List<Group> getUserGroups(String userId) {
        return groupRepository.findGroupsByUserId(userId);
    }

    // public void updateGroup(...)
    // public void deleteGroup(...)

    // // Search Functionalty
    public List<Group> searchPublicGroups(String query) {
        return groupRepository.findPublicGroupsByNameContaining(query);
    }
    // public List<Group> getPublicGroups()
}