package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadModelService {
    private static final Logger logger = LoggerFactory.getLogger(ReadModelService.class);
    private final EventStoreService eventStoreService;
    private final MongoTemplate mongoTemplate;

    public ReadModelService(EventStoreService eventStoreService, MongoTemplate mongoTemplate) {
        this.eventStoreService = eventStoreService;
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<Group> getGroup(String groupId) {
        return Optional.ofNullable(mongoTemplate.findById(groupId, Group.class));
    }

    public List<Group> getUserGroups(String userId) {
        Query query = new Query(Criteria.where("members.userId").is(userId));
        return mongoTemplate.find(query, Group.class);
    }

    public void rebuildState(String groupId) {
        List<Event> events = eventStoreService.getEvents(groupId);
        Group groupData = new Group(groupId);

        for (Event event : events) {
            applyEvent(groupData, event);
        }

        mongoTemplate.save(groupData);
        logger.info("Rebuilt state for group: {}", groupId);
    }

    private void applyEvent(Group groupData, Event event) {
        Map<String, Object> data = event.getData();
        
        switch (event.getType()) {
            case "GroupCreated":
                applyGroupCreatedEvent(groupData, data);
                break;
            case "UserJoinedGroup":
                applyUserJoinedGroupEvent(groupData, data);
                break;
            case "UserLeftGroup":
                applyUserLeftGroupEvent(groupData, data);
                break;
            case "ContributionMade":
                applyContributionMadeEvent(groupData, data);
                break;
            case "PayoutMade":
                applyPayoutMadeEvent(groupData, data);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getType());
        }
    }

    private void applyGroupCreatedEvent(Group groupData, Map<String, Object> data) {
        groupData.setGroupId((String) data.get("groupId"));
        groupData.setName((String) data.get("name"));
        groupData.setAdminId((String) data.get("adminId"));
        groupData.setMinContribution(((Number) data.get("minContribution")).doubleValue());
        groupData.setMaxMembers(((Number) data.get("maxMembers")).intValue());
        groupData.setDescription((String) data.get("description"));
        groupData.setProfileImage((String) data.get("profileImage"));
        groupData.setVisibility((String) data.get("visibility"));
        groupData.setContributionFrequency((String) data.get("contributionFrequency"));
        groupData.setPayoutFrequency((String) data.get("payoutFrequency"));
        
        // Handle balance
        Object balanceObj = data.get("balance");
        if (balanceObj != null) {
            groupData.setBalance(((Number) balanceObj).doubleValue());
        } else {
            groupData.setBalance(0.0);
        }
        
        // Handle dates
        String contributionDateStr = (String) data.get("contributionDate");
        if (contributionDateStr != null && !contributionDateStr.isEmpty()) {
            groupData.setContributionDate(new Date(contributionDateStr));
        }
        
        String payoutDateStr = (String) data.get("payoutDate");
        if (payoutDateStr != null && !payoutDateStr.isEmpty()) {
            groupData.setPayoutDate(new Date(payoutDateStr));
        }
        
        // Handle createdAt
        Object createdAtObj = data.get("createdAt");
        if (createdAtObj instanceof Date) {
            groupData.setCreatedAt((Date) createdAtObj);
        } else if (createdAtObj instanceof String) {
            groupData.setCreatedAt(new Date((String) createdAtObj));
        } else {
            groupData.setCreatedAt(new Date());
        }
        
        // Handle members
        List<Map<String, Object>> membersData = (List<Map<String, Object>>) data.get("members");
        if (membersData != null) {
            List<Group.Member> members = new ArrayList<>();
            for (Map<String, Object> memberData : membersData) {
                Group.Member member = new Group.Member();
                member.setUserId((String) memberData.get("userId"));
                member.setRole((String) memberData.get("role"));
                
                Object contributionObj = memberData.get("contribution");
                if (contributionObj != null) {
                    member.setContribution(((Number) contributionObj).doubleValue());
                } else {
                    member.setContribution(0.0);
                }
                
                Object joinedAtObj = memberData.get("joinedAt");
                if (joinedAtObj instanceof Date) {
                    member.setJoinedAt((Date) joinedAtObj);
                } else if (joinedAtObj instanceof String) {
                    member.setJoinedAt(new Date((String) joinedAtObj));
                } else {
                    member.setJoinedAt(new Date());
                }
                
                Object lastActiveObj = memberData.get("lastActive");
                if (lastActiveObj instanceof Date) {
                    member.setLastActive((Date) lastActiveObj);
                } else if (lastActiveObj instanceof String) {
                    member.setLastActive(new Date((String) lastActiveObj));
                } else {
                    member.setLastActive(new Date());
                }
                
                members.add(member);
            }
            groupData.setMembers(members);
        } else {
            groupData.setMembers(new ArrayList<>());
        }
        
        logger.info("Applied GroupCreated event for group: {}", groupData.getGroupId());
    }

    private void applyUserJoinedGroupEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        String role = (String) data.getOrDefault("role", "member");
        
        if (groupData.getMembers() == null) {
            groupData.setMembers(new ArrayList<>());
        }
        
        // Check if user is already a member
        boolean alreadyMember = groupData.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));
        
        if (!alreadyMember) {
            Group.Member newMember = new Group.Member(userId, role);
            groupData.getMembers().add(newMember);
            logger.info("User {} joined group: {}", userId, groupData.getGroupId());
        }
    }

    private void applyUserLeftGroupEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        
        if (groupData.getMembers() != null) {
            groupData.getMembers().removeIf(member -> member.getUserId().equals(userId));
            logger.info("User {} left group: {}", userId, groupData.getGroupId());
        }
    }

    private void applyContributionMadeEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        Double amount = ((Number) data.get("amount")).doubleValue();
        
        if (groupData.getMembers() != null) {
            groupData.getMembers().stream()
                    .filter(member -> member.getUserId().equals(userId))
                    .findFirst()
                    .ifPresent(member -> {
                        member.setContribution(member.getContribution() + amount);
                        member.setLastActive(new Date());
                    });
        }
        
        // Update group balance
        Double currentBalance = groupData.getBalance() != null ? groupData.getBalance() : 0.0;
        groupData.setBalance(currentBalance + amount);
        
        logger.info("Contribution of {} made by user {} to group: {}", amount, userId, groupData.getGroupId());
    }

    private void applyPayoutMadeEvent(Group groupData, Map<String, Object> data) {
        String recipientId = (String) data.get("recipientId");
        Double amount = ((Number) data.get("amount")).doubleValue();
        
        // Update group balance
        Double currentBalance = groupData.getBalance() != null ? groupData.getBalance() : 0.0;
        groupData.setBalance(Math.max(0.0, currentBalance - amount));
        
        logger.info("Payout of {} made to user {} from group: {}", amount, recipientId, groupData.getGroupId());
    }

    public List<String> getGroupMemberIds(String groupId) {
        Optional<Group> groupOpt = getGroup(groupId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            return group.getMembers().stream()
                    .map(Group.Member::getUserId)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public boolean isUserMemberOfGroup(String groupId, String userId) {
        Optional<Group> groupOpt = getGroup(groupId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            return group.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(userId));
        }
        return false;
    }
}