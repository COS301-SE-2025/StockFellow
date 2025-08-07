package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.model.Group.JoinRequest;
import com.stockfellow.groupservice.model.Group.Member;
import com.stockfellow.groupservice.repository.GroupRepository;
import com.stockfellow.groupservice.dto.NextPayeeResult;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberService {
    private static final Logger logger = LoggerFactory.getLogger(GroupMemberService.class);
    
    private final GroupRepository groupRepository;
    private final EventStoreService eventStoreService;
    private final MongoTemplate mongoTemplate;

    public GroupMemberService(GroupRepository groupRepository, EventStoreService eventStoreService, MongoTemplate mongoTemplate) {
        this.groupRepository = groupRepository;
        this.eventStoreService = eventStoreService;
        this.mongoTemplate = mongoTemplate;
    }

    // Creates Join request
    public String requestToJoinGroup(String groupId, String userId, String username){
        logger.info("User {} with username {} requesting to join group {}", userId, username, groupId);

        Group group = groupRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));

        validateJoinRequest(group, userId);

        JoinRequest joinRequest = new JoinRequest(userId, username);
        
        // Add request to groups requests array
        Query query = new Query(Criteria.where("groupId").is(groupId));
        Update update = new Update().push("requests", joinRequest);
        mongoTemplate.updateFirst(query, update, Group.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("userId", userId);
        eventData.put("requestId", joinRequest.getRequestId());
        eventData.put("action", "requested");
        
        Event event = new Event("JoinRequestCreated", eventData);
        String eventId = eventStoreService.saveEvent(groupId, event);

        logger.info("Join request created: {} for user {} in group {}", joinRequest.getRequestId(), userId, groupId);
        return eventId;
    }

    // Accept or reject join request
    public String processJoinRequest(String groupId, String requestId, String action, String adminId){
        logger.info("Admin {} processing join request {} for group {} with action: {}", adminId, requestId, groupId, action);

        if (!Arrays.asList("accept", "reject").contains(action)) {
            throw new IllegalArgumentException("Action must be 'accept' or 'reject'");
        }

        Group group = groupRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));

        validateAdminPermissions(group, adminId);

        JoinRequest joinRequest = group.getRequests().stream()
                .filter(req -> req.getRequestId().equals(requestId) && "waiting".equals(req.getState()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Join request not found or already processed"));

        String userId = joinRequest.getUserId();
        String username = joinRequest.getUsername();

        if ("accept".equals(action)) {
            // Check if group is full
            if (group.getMembers().size() >= group.getMaxMembers()) {
                throw new IllegalArgumentException("Group is full");
            }

            addMemberToGroup(groupId, userId, username);
        } else {
            rejectRequest(groupId, userId, adminId);
        }

        updateJoinRequestStatus(groupId, requestId, action);

        // Create and save event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("userId", userId);
        eventData.put("requestId", requestId);
        eventData.put("action", action);
        eventData.put("processedBy", adminId);

        Event event = new Event("JoinRequestProcessed", eventData);
        String eventId = eventStoreService.saveEvent(groupId, event);

        logger.info("Join request {} {} by admin {} for user {} in group {}", 
                   requestId, action, adminId, userId, groupId);
        return eventId;
    }

    // Fetch "waiting" join request for a group
    public List<JoinRequest> getGroupJoinRequests(String groupId){
        Group group = groupRepository.findByGroupId(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return group.getRequests().stream()
                .filter(request -> "waiting".equals(request.getState()))
                .collect(java.util.stream.Collectors.toList());
    }

    // public void removeMember(String groupId, String userId)
    // public void updateMemberRole(String groupId, String userId, String role)


    //Helpers
    private void validateJoinRequest(Group group, String userId) {
        if (!"Public".equals(group.getVisibility())) {
            throw new IllegalArgumentException("Cannot request to join private groups");
        }

        // Check if user is already a member
        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));
        
        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        // Check if user already has a pending request
        boolean hasPendingRequest = group.getRequests().stream()
                .anyMatch(request -> request.getUserId().equals(userId) && "waiting".equals(request.getState()));
        
        if (hasPendingRequest) {
            throw new IllegalArgumentException("User already has a pending request for this group");
        }

        if (group.getMembers().size() >= group.getMaxMembers()) {
            throw new IllegalArgumentException("Group is full");
        }

        // Check for recent rejections
        if (hasRecentRejection(group.getGroupId(), userId, 7)) { // 7-day cooldown
            throw new IllegalArgumentException("You were recently rejected from this group. Please wait before requesting again.");
        }

        long rejectionCount = getRejectionCount(group.getGroupId(), userId);
        if (rejectionCount >= 3) {
            throw new IllegalArgumentException("You have been rejected from this group multiple times and cannot request to join again.");
        }
    }

    private void validateAdminPermissions(Group group, String adminId) {
        boolean isAdmin = group.getAdminId().equals(adminId) || 
                         group.getMembers().stream()
                                 .anyMatch(member -> member.getUserId().equals(adminId) && 
                                          Arrays.asList("admin", "founder").contains(member.getRole()));
        
        if (!isAdmin) {
            throw new IllegalArgumentException("Only group admins can process join requests");
        }
    }

    public void addMemberToGroup(String groupId, String userId, String username) {
        Member newMember = new Member(userId, username, "member");
        
        Query query = new Query(Criteria.where("groupId").is(groupId));
        Update update = new Update().push("members", newMember);
        mongoTemplate.updateFirst(query, update, Group.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("userId", userId);
        eventData.put("username", username);
        eventData.put("role", "member");

        Event event = new Event("MemberAdded", eventData);
        eventStoreService.saveEvent(groupId, event);
    }

    private void updateJoinRequestStatus(String groupId, String requestId, String action) {
        String newState = "accept".equals(action) ? "accepted" : "rejected";
        
        Query query = new Query(Criteria.where("groupId").is(groupId)
                .and("requests.requestId").is(requestId));
        Update update = new Update().set("requests.$.state", newState);
        mongoTemplate.updateFirst(query, update, Group.class);
    }

    private void rejectRequest(String groupId, String userId, String adminId) {
        // Create rejection event with more detailed information
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("userId", userId);
        eventData.put("rejectedBy", adminId);
        eventData.put("rejectedAt", new Date());

        Event event = new Event("JoinRequestRejected", eventData);
        eventStoreService.saveEvent(groupId, event);
        
        logger.info("Join request rejected for user {} in group {} by admin {}", userId, groupId, adminId);
    }

    /**
     * Get the next member to receive payout for a group
     */
    public NextPayeeResult getNextPayee(String groupId) {
        logger.info("Getting next payee for group {}", groupId);

        // Get the group
        Group group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Initialize payout order if not set
        logger.info("Getting next payee for group {}", groupId);
        if (group.getPayoutOrder() == null || group.getPayoutOrder().isEmpty()) {
            group.initializePayoutOrder();
            groupRepository.save(group);
        }

        // Get next payee
        logger.info("Getting next payee for group {}", groupId);
        String nextRecipientId = group.getNextPayoutRecipient();
        if (nextRecipientId == null) {
            throw new IllegalStateException("No members available for payout in this group");
        }

        // Get recipient details
        logger.info("Getting next payee for group {}", groupId);
        Group.Member recipient = group.getMembers().stream()
                .filter(member -> member.getUserId().equals(nextRecipientId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Next payee not found in members list"));

        return new NextPayeeResult(
                groupId,
                group.getName(),
                nextRecipientId,
                recipient.getUsername(),
                recipient.getRole(),
                group.getCurrentPayoutPosition() != null ? group.getCurrentPayoutPosition() : 0,
                group.getPayoutOrder().size(),
                group.getBalance(),
                group.getLastPayoutRecipient(),
                group.getLastPayoutDate(),
                group.getPayoutFrequency(),
                group.getPayoutDate()
        );
    } 

    /**
     * Record that a payout has been made and advance to next member
     */
    public NextPayeeResult recordPayout(String groupId, String recipientId, Double amount) {
        logger.info("Recording payout of {} to {} for group {}", amount, recipientId, groupId);

        // Get the group
        Group group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Verify the recipient is correct
        String expectedRecipient = group.getNextPayoutRecipient();
        if (!recipientId.equals(expectedRecipient)) {
            throw new IllegalArgumentException("Payout recipient mismatch. Expected: " + expectedRecipient + ", Got: " + recipientId);
        }

        // Update payout tracking using MongoDB atomic update
        Query query = new Query(Criteria.where("groupId").is(groupId));
        Update update = new Update()
                .set("lastPayoutRecipient", recipientId)
                .set("lastPayoutDate", new Date())
                .inc("currentPayoutPosition", 1);

        mongoTemplate.updateFirst(query, update, Group.class);

        // Create payout event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("groupId", groupId);
        eventData.put("recipientId", recipientId);
        eventData.put("amount", amount);
        eventData.put("payoutDate", new Date());

        Event event = new Event("PayoutProcessed", eventData);
        eventStoreService.saveEvent(groupId, event);

        logger.info("Recorded payout of {} to {} for group {}", amount, recipientId, groupId);

        // Return info for next payout
        return getNextPayee(groupId);
    }

    /*
     * Check if a user has been rejected from a group recently
     */
    public boolean hasRecentRejection(String groupId, String userId) {
        return hasRecentRejection(groupId, userId, 30); // 30 days
    }

    /**
     * Check if a user has been rejected from a group within specified days
     */
    public boolean hasRecentRejection(String groupId, String userId, int daysAgo) {
        List<Event> events = eventStoreService.getEventsByType(groupId, "JoinRequestRejected");
        
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.DAY_OF_MONTH, -daysAgo);
        
        return events.stream()
                .filter(event -> event.getTimestamp().after(cutoffDate.getTime()))
                .anyMatch(event -> userId.equals(event.getData().get("userId")));
    }

    /**
     * Get rejection count for a user in a specific group
     */
    public long getRejectionCount(String groupId, String userId) {
        List<Event> events = eventStoreService.getEventsByType(groupId, "JoinRequestRejected");
        
        return events.stream()
                .filter(event -> userId.equals(event.getData().get("userId")))
                .count();
    }
}
