package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.repository.GroupRepository;
import com.stockfellow.groupservice.dto.CreateGroupRequest;
import com.stockfellow.groupservice.dto.CreateGroupResult;
import com.stockfellow.groupservice.dto.UpdateGroupRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GroupService {
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);
    private final GroupMemberService groupMemberService;
    private final EventStoreService eventStoreService;
    private final GroupRepository groupRepository;

    public GroupService(EventStoreService eventStoreService,
            GroupRepository groupRepository,
            GroupMemberService groupMemberService) {
        this.eventStoreService = eventStoreService;
        this.groupRepository = groupRepository;
        this.groupMemberService = groupMemberService;
    }

    public CreateGroupResult createGroup(CreateGroupRequest request) {
        logger.info("Creating group '{}' for adminId: {} with username: {}", request.getName(), request.getAdminId(),
                request.getAdminName());

        validateCreateGroupRequest(request);

        String groupId = generateGroupId();

        List<String> members = new ArrayList<>(request.getMembers());
        if (!members.contains(request.getAdminId())) {
            members.add(0, request.getAdminId());
        }

        Map<String, Object> eventData = buildGroupCreatedEventData(groupId, request, members);
        Event event = new Event("GroupCreated", eventData);
        String eventId = eventStoreService.saveEvent(groupId, event);

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
            throw new IllegalArgumentException(
                    "Invalid contribution frequency. Must be: Monthly, Bi-weekly, or Weekly");
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

    private Map<String, Object> buildGroupCreatedEventData(String groupId, CreateGroupRequest request,
            List<String> members) {
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
            String username = memberId.equals(request.getAdminId()) ? request.getAdminName() : memberId;
            members.add(new Group.Member(memberId, username, role));
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

    public Group updateGroup(String groupId, UpdateGroupRequest updateRequest) {
        Group group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Validate maxMembers if being updated
        if (updateRequest.getMaxMembers() != null) {
            if (updateRequest.getMaxMembers() <= 0) {
                throw new IllegalArgumentException("Maximum members must be greater than 0");
            }
            if (group.getMembers().size() > updateRequest.getMaxMembers()) {
                throw new IllegalArgumentException("Cannot set max members below current member count");
            }
        }

        // Validate visibility if being updated
        if (updateRequest.getVisibility() != null &&
                !Arrays.asList("Public", "Private").contains(updateRequest.getVisibility())) {
            throw new IllegalArgumentException("Visibility must be 'Public' or 'Private'");
        }

        // Validate frequencies if being updated
        if (updateRequest.getContributionFrequency() != null &&
                !Arrays.asList("Weekly", "Bi-weekly", "Monthly").contains(updateRequest.getContributionFrequency())) {
            throw new IllegalArgumentException("Invalid contribution frequency");
        }
        if (updateRequest.getPayoutFrequency() != null &&
                !Arrays.asList("Weekly", "Bi-weekly", "Monthly").contains(updateRequest.getPayoutFrequency())) {
            throw new IllegalArgumentException("Invalid payout frequency");
        }

        // Update fields if they're provided in the request
        if (updateRequest.getName() != null)
            group.setName(updateRequest.getName());
        if (updateRequest.getMaxMembers() != null)
            group.setMaxMembers(updateRequest.getMaxMembers());
        if (updateRequest.getDescription() != null)
            group.setDescription(updateRequest.getDescription());
        if (updateRequest.getProfileImage() != null)
            group.setProfileImage(updateRequest.getProfileImage());
        if (updateRequest.getVisibility() != null)
            group.setVisibility(updateRequest.getVisibility());
        if (updateRequest.getContributionFrequency() != null)
            group.setContributionFrequency(updateRequest.getContributionFrequency());
        if (updateRequest.getContributionDate() != null)
            group.setContributionDate(updateRequest.getContributionDate());
        if (updateRequest.getPayoutFrequency() != null)
            group.setPayoutFrequency(updateRequest.getPayoutFrequency());
        if (updateRequest.getPayoutDate() != null)
            group.setPayoutDate(updateRequest.getPayoutDate());

        // Save the updated group
        Group savedGroup = groupRepository.save(group);

        // Create and save update event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("updatedFields", updateRequest);
        eventData.put("updatedAt", new Date());

        Event event = new Event("GroupUpdated", eventData);
        eventStoreService.saveEvent(groupId, event);

        logger.info("Group {} updated by admin", groupId);
        return savedGroup;
    }
    // public void deleteGroup(...)

    // // Search Functionalty
    public List<Group> searchPublicGroups(String query) {
        return groupRepository.findPublicGroupsByNameContaining(query);
    }
    // public List<Group> getPublicGroups()

    public CreateGroupResult createGroupForTier(Integer tier, String userId, String username) {
        // Find oldest non-full group in this tier
        Optional<Group> availableGroup = findAvailableGroupInTier(tier);

        if (availableGroup.isPresent()) {
            // Add user to existing group using GroupMemberService
            Group group = availableGroup.get();
            try {
                groupMemberService.addMemberToGroup(group.getGroupId(), userId, username);
                return new CreateGroupResult(group.getGroupId(), null, "Added to existing group");
            } catch (Exception e) {
                logger.error("Failed to add member to group: {}", e.getMessage());
                throw new RuntimeException("Failed to add member to existing group");
            }
        } else {
            // Create new group for this tier
            return createNewGroupForTier(tier, userId, username);
        }
    }

    private Optional<Group> findAvailableGroupInTier(Integer tier) {
        // Find all public(system created groups) groups in this tier that aren't full
        List<Group> groups = groupRepository.findByTierAndVisibility(tier, "Public");

        return groups.stream()
                .filter(g -> g.getMembers().size() < g.getMaxMembers())
                .sorted(Comparator.comparing(Group::getCreatedAt))
                .findFirst();
    }

    private CreateGroupResult createNewGroupForTier(Integer tier, String adminId, String adminName) {
        Double minContribution = Group.TIER_RANGES.get(tier)[0];

        CreateGroupRequest request = new CreateGroupRequest();
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setName(generateStokvelName());
        request.setMinContribution(minContribution);
        request.setMaxMembers(10); // Fixed at 10 members
        request.setVisibility("Public");
        request.setContributionFrequency("Monthly");
        request.setPayoutFrequency("Monthly");
        request.setTier(tier);

        // Current date for contribution/payout dates
        Date now = new Date();
        request.setContributionDate(now);
        request.setPayoutDate(now);

        return createGroup(request);
    }

    private String generateStokvelName() {
        // Alternative implementation since countByNameStartingWith isn't available
        // Using a simple timestamp-based approach
        return "Stokvel #" + System.currentTimeMillis();
    }
}