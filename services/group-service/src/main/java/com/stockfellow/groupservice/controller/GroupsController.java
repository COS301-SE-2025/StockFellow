package com.stockfellow.groupservice.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.stockfellow.groupservice.command.CreateGroupCommand;
import com.stockfellow.groupservice.command.JoinGroupCommand;
import com.stockfellow.groupservice.command.ProcessJoinRequestCommand;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.service.ReadModelService;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {
    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);
    private final CreateGroupCommand createGroupCommand;
    private final JoinGroupCommand joinGroupCommand;
    private final ProcessJoinRequestCommand processJoinRequestCommand;
    private final ReadModelService readModelService;
    private final EventStoreService eventStoreService;
    private final SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public GroupsController(CreateGroupCommand createGroupCommand,
                            JoinGroupCommand joinGroupCommand,
                            ProcessJoinRequestCommand processJoinRequestCommand,
                            ReadModelService readModelService,
                            EventStoreService eventStoreService) {
        this.createGroupCommand = createGroupCommand;
        this.joinGroupCommand = joinGroupCommand;
        this.processJoinRequestCommand = processJoinRequestCommand;
        this.readModelService = readModelService;
        this.eventStoreService = eventStoreService;
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @GetMapping
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Group Service");
        response.put("version", "1.0.0");
        response.put("endpoints", Arrays.asList(
                "POST /api/groups/create - Create a new group",
                "GET /api/groups/user - Get groups for authenticated user",
                "GET /api/groups/{groupId}/view - View group details and events",
                "POST /api/groups/{groupId}/join - Request to join a group",
                "POST /api/groups/{groupId}/request - Process join request (accept/reject)"
        ));
        return response;
    }

    @GetMapping("/{groupId}/view")
    public ResponseEntity<?> viewGroup(@PathVariable String groupId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String userId = auth.getPrincipal().toString();

            // Get group details
            Optional<Group> groupOpt = readModelService.getGroup(groupId);
            if (!groupOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Group not found"));
            }

            Group group = groupOpt.get();
            
            // Check if user has permission to view group details
            boolean isMember = group.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(userId));
            boolean isAdmin = group.getAdminId().equals(userId);
            
            // For private groups, only members and admins can view full details
            if ("Private".equals(group.getVisibility()) && !isMember && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. You must be a member to view this private group."));
            }

            // Get all events for this group
            List<Event> events = eventStoreService.getEvents(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("group", group);
            response.put("events", events);
            response.put("userPermissions", Map.of(
                    "isMember", isMember,
                    "isAdmin", isAdmin,
                    "canViewRequests", isAdmin || (isMember && ("founder".equals(getMemberRole(group, userId)) || "admin".equals(getMemberRole(group, userId))))
            ));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error viewing group {}: {}", groupId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Helper method to extract user ID from the authentication object.
     * 
     * @param auth The authentication object.
     * @return The user ID or null if not found.
     */
    private String getUserIdFromAuthentication(Authentication auth) {
        if (auth == null) {
            return null;
        }
        
        // First try to get from principal directly
        Object principal = auth.getPrincipal();
        if (principal instanceof String && !principal.equals("anonymousUser")) {
            return (String) principal;
        }
        
        // If principal is not a string or is anonymousUser, try to get from JWT details
        if (auth.getDetails() instanceof DecodedJWT) {
            DecodedJWT jwt = (DecodedJWT) auth.getDetails();
            return jwt.getSubject();
        }
        
        // Fallback to name
        String name = auth.getName();
        if (name != null && !name.equals("anonymousUser")) {
            return name;
        }
        
        return null;
    }

    /**
     * Create a new group with the provided details.
     * 
     * @param request The request body containing group details.
     * @return ResponseEntity with the result of the group creation.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            
            String adminId = getUserIdFromAuthentication(auth);
            if (adminId.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Valid authentication required - anonymous user cannot create groups"));
            }

            if (adminId == null || adminId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User ID not found in authentication"));
            }

            // Extract fields from request matching frontend payload
            String name = (String) request.get("name");
            Object minContributionObj = request.get("minContribution");
            Object maxMembersObj = request.get("maxMembers");
            String description = (String) request.get("description");
            String profileImage = (String) request.get("profileImage");
            String visibility = (String) request.get("visibility");
            String contributionFrequency = (String) request.get("contributionFrequency");
            String contributionDateStr = (String) request.get("contributionDate");
            String payoutFrequency = (String) request.get("payoutFrequency");
            String payoutDateStr = (String) request.get("payoutDate");
            List<String> members = (List<String>) request.getOrDefault("members", new ArrayList<>());

            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Group name is required"));
            }
            if (minContributionObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Minimum contribution is required"));
            }
            if (maxMembersObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Maximum members is required"));
            }
            if (visibility == null || visibility.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Visibility is required"));
            }
            if (contributionFrequency == null || contributionFrequency.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Contribution frequency is required"));
            }
            if (payoutFrequency == null || payoutFrequency.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payout frequency is required"));
            }

            // Parse numeric values
            Double minContribution;
            Integer maxMembers;
            
            try {
                minContribution = minContributionObj instanceof Number ?
                        ((Number) minContributionObj).doubleValue() :
                        Double.parseDouble(minContributionObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid minimum contribution format"));
            }

            try {
                maxMembers = maxMembersObj instanceof Number ?
                        ((Number) maxMembersObj).intValue() :
                        Integer.parseInt(maxMembersObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid maximum members format"));
            }

            // Validate values
            if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(contributionFrequency)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid contribution frequency. Must be: Monthly, Bi-weekly, or Weekly"));
            }
            if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(payoutFrequency)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid payout frequency. Must be: Monthly, Bi-weekly, or Weekly"));
            }
            if (!Arrays.asList("Private", "Public").contains(visibility)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid visibility. Must be: Private or Public"));
            }
            if (minContribution <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Minimum contribution must be greater than 0"));
            }
            if (maxMembers <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Maximum members must be greater than 0"));
            }
            if (members.size() > maxMembers) {
                return ResponseEntity.badRequest().body(Map.of("error", "Number of initial members cannot exceed maximum members"));
            }

            // Parse dates from ISO format
            Date contributionDate = null;
            Date payoutDate = null;
            
            if (contributionDateStr != null && !contributionDateStr.trim().isEmpty()) {
                try {
                    contributionDate = isoFormatter.parse(contributionDateStr);
                } catch (ParseException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid contribution date format. Expected ISO format"));
                }
            }
            
            if (payoutDateStr != null && !payoutDateStr.trim().isEmpty()) {
                try {
                    payoutDate = isoFormatter.parse(payoutDateStr);
                } catch (ParseException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid payout date format. Expected ISO format"));
                }
            }

            // Generate unique group ID
            String groupId = "group_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

            // Initialize members with admin as the first member if empty
            if (members.isEmpty()) {
                members.add(adminId);
            } else if (!members.contains(adminId)) {
                // Ensure admin is always included
                members.add(0, adminId); // Add admin at the beginning
            }

            // Execute command
            String eventId = createGroupCommand.execute(groupId, adminId, name, minContribution, maxMembers,
                    description, profileImage, visibility, contributionFrequency, contributionDate,
                    payoutFrequency, payoutDate, members);
            
            logger.info("Group created with ID: {} by admin: {}", groupId, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Group created successfully");
            response.put("groupId", groupId);
            response.put("eventId", eventId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Group creation validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during group creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserGroups() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String userId = auth.getPrincipal().toString();

            List<Group> groups = readModelService.getUserGroups(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error fetching user groups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> requestToJoinGroup(@PathVariable String groupId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String userId = auth.getPrincipal().toString();

            // Check if group exists
            Group group = readModelService.getGroup(groupId)
                    .orElseThrow(() -> new IllegalStateException("Group not found"));
            
            // Check if user is already a member
            boolean alreadyMember = group.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(userId));
            
            if (alreadyMember) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is already a member of this group"));
            }
            
            // Check if user already has a pending request
            boolean hasPendingRequest = group.getRequests().stream()
                    .anyMatch(request -> request.getUserId().equals(userId) && "waiting".equals(request.getState()));
            
            if (hasPendingRequest) {
                return ResponseEntity.badRequest().body(Map.of("error", "You already have a pending request for this group"));
            }
            
            // Check if group is full
            if (group.getMembers().size() >= group.getMaxMembers()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Group is full"));
            }

            // For public groups, add user directly; for private groups, create a request
            String eventId;
            if ("Public".equals(group.getVisibility())) {
                eventId = joinGroupCommand.execute(groupId, userId);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully joined group");
                response.put("groupId", groupId);
                response.put("eventId", eventId);
                return ResponseEntity.ok(response);
            } else {
                eventId = joinGroupCommand.createJoinRequest(groupId, userId);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Join request sent to group admins");
                response.put("groupId", groupId);
                response.put("eventId", eventId);
                return ResponseEntity.ok(response);
            }
            
        } catch (IllegalStateException e) {
            logger.error("Error requesting to join group: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during group join request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{groupId}/request")
    public ResponseEntity<?> processJoinRequest(@PathVariable String groupId, @RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String adminId = auth.getPrincipal().toString();

            String requestId = (String) request.get("requestId");
            String action = (String) request.get("action"); // "accept" or "reject"

            if (requestId == null || requestId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request ID is required"));
            }
            if (action == null || (!action.equals("accept") && !action.equals("reject"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Action must be 'accept' or 'reject'"));
            }

            // Check if group exists and user is admin
            Group group = readModelService.getGroup(groupId)
                    .orElseThrow(() -> new IllegalStateException("Group not found"));
            
            boolean isAdmin = group.getAdminId().equals(adminId) || 
                            group.getMembers().stream()
                                    .anyMatch(member -> member.getUserId().equals(adminId) && 
                                             ("admin".equals(member.getRole()) || "founder".equals(member.getRole())));
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only group admins can process join requests"));
            }

            String eventId = processJoinRequestCommand.execute(groupId, requestId, action, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Join request " + action + "ed successfully");
            response.put("groupId", groupId);
            response.put("requestId", requestId);
            response.put("action", action);
            response.put("eventId", eventId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            logger.error("Error processing join request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during join request processing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    private String getMemberRole(Group group, String userId) {
        return group.getMembers().stream()
                .filter(member -> member.getUserId().equals(userId))
                .map(Group.Member::getRole)
                .findFirst()
                .orElse(null);
    }
}