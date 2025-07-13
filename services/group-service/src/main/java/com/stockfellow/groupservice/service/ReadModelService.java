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
        Query query = new Query(Criteria.where("groupId").is(groupId));
        Group group = mongoTemplate.findOne(query, Group.class);
        return Optional.ofNullable(group);
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
            case "UserJoinedGroup": //System Groups
            case "MemberAdded":     //Custom Groups
                applyMemberAddedEvent(groupData, data);
                break;
            case "UserLeftGroup":
            case "MemberRemoved":
                applyMemberRemovedEvent(groupData, data);
                break;
            case "MemberRoleUpdated":
                applyMemberRoleUpdatedEvent(groupData, data);
                break;
            case "JoinRequestCreated":
                applyJoinRequestCreatedEvent(groupData, data);
                break;
            case "JoinRequestProcessed":
                applyJoinRequestProcessedEvent(groupData, data);
                break;
            case "JoinRequestRejected":
                logger.debug("Join request rejected for user {} in group {}", 
                           data.get("userId"), data.get("groupId"));
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
        
        // Handle dates - improved parsing
        groupData.setContributionDate(parseDate(data.get("contributionDate")));
        groupData.setPayoutDate(parseDate(data.get("payoutDate")));
        groupData.setCreatedAt(parseDate(data.get("createdAt"), new Date()));
        
        // Handle members - support both string list and member object list
        Object membersObj = data.get("members");
        List<Group.Member> members = new ArrayList<>();
        
        if (membersObj instanceof List) {
            List<?> membersList = (List<?>) membersObj;
            for (Object memberObj : membersList) {
                if (memberObj instanceof String) {
                    // Handle string list (initial member IDs)
                    String userId = (String) memberObj;
                    String role = userId.equals(data.get("adminId")) ? "founder" : "member";
                    members.add(new Group.Member(userId, role));
                } else if (memberObj instanceof Map) {
                    // Handle member object list
                    @SuppressWarnings("unchecked")
                    Map<String, Object> memberData = (Map<String, Object>) memberObj;
                    Group.Member member = new Group.Member();
                    member.setUserId((String) memberData.get("userId"));
                    member.setRole((String) memberData.getOrDefault("role", "member"));
                    member.setContribution(parseDouble(memberData.get("contribution"), 0.0));
                    member.setJoinedAt(parseDate(memberData.get("joinedAt"), new Date()));
                    member.setLastActive(parseDate(memberData.get("lastActive"), new Date()));
                    members.add(member);
                }
            }
        }
        
        groupData.setMembers(members);
        groupData.setRequests(new ArrayList<>()); // Initialize empty requests list
        
        logger.info("Applied GroupCreated event for group: {}", groupData.getGroupId());
    }

    private void applyMemberAddedEvent(Group groupData, Map<String, Object> data) {
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

    private void applyMemberRemovedEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        
        if (groupData.getMembers() != null) {
            groupData.getMembers().removeIf(member -> member.getUserId().equals(userId));
            logger.info("User {} left group: {}", userId, groupData.getGroupId());
        }
    }

    private void applyMemberRoleUpdatedEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        String newRole = (String) data.get("newRole");
        
        if (groupData.getMembers() != null) {
            groupData.getMembers().stream()
                    .filter(member -> member.getUserId().equals(userId))
                    .findFirst()
                    .ifPresent(member -> {
                        String oldRole = member.getRole();
                        member.setRole(newRole);
                        member.setLastActive(new Date());
                        logger.info("User {} role updated from {} to {} in group: {}", 
                                  userId, oldRole, newRole, groupData.getGroupId());
                    });
        }
    }

    private void applyJoinRequestCreatedEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        String requestId = (String) data.get("requestId");
        
        if (groupData.getRequests() == null) {
            groupData.setRequests(new ArrayList<>());
        }
        
        // Check if request already exists
        boolean requestExists = groupData.getRequests().stream()
                .anyMatch(request -> request.getRequestId().equals(requestId));
        
        if (!requestExists) {
            Group.JoinRequest joinRequest = new Group.JoinRequest(userId);
            joinRequest.setRequestId(requestId);
            joinRequest.setState("waiting");
            groupData.getRequests().add(joinRequest);
            logger.info("Join request {} created for user {} in group: {}", 
                       requestId, userId, groupData.getGroupId());
        }
    }

    private void applyJoinRequestProcessedEvent(Group groupData, Map<String, Object> data) {
        String requestId = (String) data.get("requestId");
        String action = (String) data.get("action");
        String userId = (String) data.get("userId");
        
        if (groupData.getRequests() != null) {
            groupData.getRequests().stream()
                    .filter(request -> request.getRequestId().equals(requestId))
                    .findFirst()
                    .ifPresent(request -> {
                        String newState = "accept".equals(action) ? "accepted" : "rejected";
                        request.setState(newState);
                        logger.info("Join request {} {} for user {} in group: {}", 
                                  requestId, newState, userId, groupData.getGroupId());
                    });
        }
        
        // If accepted, the user should already be added by a separate MemberAdded event
        // If rejected, we just update the request state above
    }

    private void applyContributionMadeEvent(Group groupData, Map<String, Object> data) {
        String userId = (String) data.get("userId");
        Double amount = parseDouble(data.get("amount"), 0.0);
        
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
        Double amount = parseDouble(data.get("amount"), 0.0);
        
        // Update group balance
        Double currentBalance = groupData.getBalance() != null ? groupData.getBalance() : 0.0;
        groupData.setBalance(Math.max(0.0, currentBalance - amount));
        
        logger.info("Payout of {} made to user {} from group: {}", amount, recipientId, groupData.getGroupId());
    }

    // Helper methods for robust data parsing
    private Date parseDate(Object dateObj) {
        return parseDate(dateObj, null);
    }

    private Date parseDate(Object dateObj, Date defaultValue) {
        if (dateObj instanceof Date) {
            return (Date) dateObj;
        } else if (dateObj instanceof String) {
            try {
                return new Date((String) dateObj);
            } catch (Exception e) {
                logger.warn("Failed to parse date string: {}", dateObj);
                return defaultValue;
            }
        } else if (dateObj instanceof Number) {
            return new Date(((Number) dateObj).longValue());
        }
        return defaultValue;
    }

    private Double parseDouble(Object obj, Double defaultValue) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse double string: {}", obj);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // Existing utility methods
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

    public boolean isUserAdminOfGroup(String groupId, String userId) {
        Optional<Group> groupOpt = getGroup(groupId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            return group.getAdminId().equals(userId) || 
                   group.getMembers().stream()
                           .anyMatch(member -> member.getUserId().equals(userId) && 
                                    Arrays.asList("admin", "founder").contains(member.getRole()));
        }
        return false;
    }

    public List<Group.JoinRequest> getPendingJoinRequests(String groupId) {
        Optional<Group> groupOpt = getGroup(groupId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            return group.getRequests().stream()
                    .filter(request -> "waiting".equals(request.getState()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}