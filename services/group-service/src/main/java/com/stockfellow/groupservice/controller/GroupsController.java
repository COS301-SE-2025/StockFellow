package com.stockfellow.groupservice.controller;

import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.service.GroupService;
import com.stockfellow.groupservice.service.GroupMemberService;
import com.stockfellow.groupservice.service.ReadModelService;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.dto.CreateGroupRequest;
import com.stockfellow.groupservice.dto.CreateGroupResult;
import com.stockfellow.groupservice.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Group management API for creating, joining, and managing investment groups")
public class GroupsController {
    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);
    
    private final GroupService groupService;
    private final GroupMemberService memberService;
    private final ReadModelService readModelService;
    private final EventStoreService eventStoreService;
    private final SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public GroupsController(GroupService groupService,
                           GroupMemberService memberService,
                           ReadModelService readModelService,
                           EventStoreService eventStoreService) {
        this.groupService = groupService;
        this.memberService = memberService;
        this.readModelService = readModelService;
        this.eventStoreService = eventStoreService;
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @GetMapping
    @Operation(
        summary = "Get service information",
        description = "Returns basic information about the Group Service and available endpoints"
    )
    @ApiResponse(responseCode = "200", description = "Service information retrieved successfully")
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Group Service");
        response.put("version", "2.0.0");
        response.put("endpoints", Arrays.asList(
                "POST /api/groups/create - Create a new group",
                "GET /api/groups/user - Get groups for authenticated user",
                "GET /api/groups/{groupId}/view - View group details and events",
                "GET /api/groups/{groupId}/join - Request to join a group (public groups only)",
                "GET /api/groups/{groupId}/requests - Get all join requests for a group (admin only)",
                "POST /api/groups/{groupId}/request - Process join request (accept/reject)",
                "GET /api/groups/search?query=<search_term> - Search public groups"
        ));
        return response;
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search public groups",
        description = "Search for public groups by name or description. If no query is provided, returns all public groups."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchPublicGroups(
            @Parameter(description = "Search term to filter groups by name or description")
            @RequestParam(required = false) String query) {
        try {
            List<Group> groups = groupService.searchPublicGroups(query);
            
            // Create response with basic group information for search results
            List<Map<String, Object>> searchResults = new ArrayList<>();
            for (Group group : groups) {
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("id", group.getId());
                groupInfo.put("groupId", group.getGroupId());
                groupInfo.put("name", group.getName());
                groupInfo.put("description", group.getDescription());
                groupInfo.put("profileImage", group.getProfileImage());
                groupInfo.put("visibility", group.getVisibility());
                groupInfo.put("minContribution", group.getMinContribution());
                groupInfo.put("maxMembers", group.getMaxMembers());
                groupInfo.put("contributionFrequency", group.getContributionFrequency());
                groupInfo.put("payoutFrequency", group.getPayoutFrequency());
                groupInfo.put("currentMembers", group.getMembers() != null ? group.getMembers().size() : 0);
                groupInfo.put("balance", group.getBalance());
                groupInfo.put("createdAt", group.getCreatedAt());
                
                // Add member status flags
                groupInfo.put("isFull", group.getMembers() != null && group.getMembers().size() >= group.getMaxMembers());
                groupInfo.put("availableSlots", group.getMaxMembers() - (group.getMembers() != null ? group.getMembers().size() : 0));
                
                searchResults.add(groupInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("groups", searchResults);
            response.put("totalCount", searchResults.size());
            response.put("query", query);
            
            logger.info("Search performed for query: '{}', found {} public groups", query, searchResults.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching groups with query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error during search"));
        }
    }

    @GetMapping("/{groupId}/view")
    @Operation(
        summary = "View group details",
        description = "Get detailed information about a group including members and events. Private groups require membership."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied to private group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<?> viewGroup(
            @Parameter(description = "Group ID to view") @PathVariable String groupId,
            HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID not found in request"));
            }

            logger.info("Fetching info for group: {}", groupId);
            Optional<Group> groupOpt = readModelService.getGroup(groupId);
            if (!groupOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Group not found"));
            }

            Group group = groupOpt.get();
            
            // Check if user has permission to view group details
            boolean isMember = group.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(userId));
            boolean isAdmin = readModelService.isUserAdminOfGroup(groupId, userId);
            
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
                    "canViewRequests", isAdmin
            ));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error viewing group {}: {}", groupId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/create")
    @Operation(
        summary = "Create a new group",
        description = "Create a new investment group with specified parameters"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Group created successfully",
                content = @Content(schema = @Schema(example = "{\"message\":\"Group created successfully\",\"groupId\":\"group_123\",\"eventId\":\"event_456\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Group creation details",
                content = @Content(
                    schema = @Schema(implementation = CreateGroupRequestDto.class),
                    examples = @ExampleObject(
                        value = "{\n" +
                                "  \"name\": \"Investment Club\",\n" +
                                "  \"minContribution\": 100.0,\n" +
                                "  \"maxMembers\": 10,\n" +
                                "  \"description\": \"Monthly investment group\",\n" +
                                "  \"visibility\": \"Public\",\n" +
                                "  \"contributionFrequency\": \"Monthly\",\n" +
                                "  \"payoutFrequency\": \"Monthly\",\n" +
                                "  \"members\": [\"user123\", \"user456\"]\n" +
                                "}"
                    )
                )
            )
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest httpRequest) {
        try {
            String adminId = httpRequest.getHeader("X-User-Id");
            if (adminId == null || adminId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID not found in request"));
            }
            
            logger.info("Creating group for adminId: {}", adminId);

            // Parse and validate the request body
            CreateGroupRequest request = parseCreateGroupRequest(requestBody, adminId);

            // Call the service to create the group
            CreateGroupResult result = groupService.createGroup(request);

            // Build successful response
            Map<String, Object> response = new HashMap<>();
            response.put("message", result.getMessage());
            response.put("groupId", result.getGroupId());
            response.put("eventId", result.getEventId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Group creation validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during group creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user")
    @Operation(
        summary = "Get user's groups",
        description = "Retrieve all groups that the authenticated user is a member of"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User groups retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserGroups(HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID not found in request"));
            }

            List<Group> groups = groupService.getUserGroups(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error fetching user groups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{groupId}/join")
    @Operation(
        summary = "Request to join a group",
        description = "Send a join request to a public group. Private groups require an invitation."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Join request sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request (already member, group full, etc.)"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Cannot join private group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<?> requestToJoinGroup(
            @Parameter(description = "ID of the group to join") @PathVariable String groupId,
            HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID not found in request"));
            }

            // Create join request using the member service
            String eventId = memberService.requestToJoinGroup(groupId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Join request sent successfully. Waiting for admin approval.");
            response.put("groupId", groupId);
            response.put("eventId", eventId);
            response.put("status", "pending");
            
            logger.info("User {} requested to join group {}", userId, groupId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error requesting to join group: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during group join request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{groupId}/requests")
    @Operation(
        summary = "Get join requests for a group",
        description = "Retrieve all pending join requests for a group. Only group admins can access this."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Join requests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<?> getGroupJoinRequests(
            @Parameter(description = "Group ID to get requests for") @PathVariable String groupId,
            HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID not found in request"));
            }

            // Check if user is admin
            if (!readModelService.isUserAdminOfGroup(groupId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Only group admins can view join requests."));
            }

            // Get pending requests
            List<Group.JoinRequest> pendingRequests = memberService.getGroupJoinRequests(groupId);

            // Get group details for response
            Optional<Group> groupOpt = readModelService.getGroup(groupId);
            if (!groupOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Group not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("groupId", groupId);
            response.put("groupName", groupOpt.get().getName());
            response.put("requests", pendingRequests);
            response.put("totalPendingRequests", pendingRequests.size());
            
            logger.info("Admin {} retrieved {} join requests for group {}", userId, pendingRequests.size(), groupId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching join requests for group {}: {}", groupId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{groupId}/request")
    @Operation(
        summary = "Process a join request",
        description = "Accept or reject a pending join request. Only group admins can perform this action."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Join request processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
        @ApiResponse(responseCode = "404", description = "Group or request not found")
    })
    public ResponseEntity<?> processJoinRequest(
            @Parameter(description = "Group ID") @PathVariable String groupId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Join request processing details",
                content = @Content(
                    examples = @ExampleObject(
                        value = "{\n" +
                                "  \"requestId\": \"req_123456\",\n" +
                                "  \"action\": \"accept\"\n" +
                                "}"
                    )
                )
            )
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String adminId = httpRequest.getHeader("X-User-Id");
            
            if (adminId == null || adminId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID not found in request"));
            }

            String requestId = (String) request.get("requestId");
            String action = (String) request.get("action"); // "accept" or "reject"

            if (requestId == null || requestId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request ID is required"));
            }
            if (action == null || (!action.equals("accept") && !action.equals("reject"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Action must be 'accept' or 'reject'"));
            }

            // Process the join request using the member service
            String eventId = memberService.processJoinRequest(groupId, requestId, action, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Join request " + action + "ed successfully");
            response.put("groupId", groupId);
            response.put("requestId", requestId);
            response.put("action", action);
            response.put("eventId", eventId);
            
            if ("accept".equals(action)) {
                response.put("result", "User has been added to the group and granted access");
            } else {
                response.put("result", "Request has been rejected and removed");
            }
            
            logger.info("Admin {} {} join request {} for group {}", adminId, action, requestId, groupId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error processing join request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during join request processing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    // Helper method to parse create group request
    private CreateGroupRequest parseCreateGroupRequest(Map<String, Object> requestBody, String adminId) {
        // Extract and validate required fields
        String name = extractStringField(requestBody, "name", true);
        Double minContribution = extractDoubleField(requestBody, "minContribution", true);
        Integer maxMembers = extractIntegerField(requestBody, "maxMembers", true);
        String visibility = extractStringField(requestBody, "visibility", true);
        String contributionFrequency = extractStringField(requestBody, "contributionFrequency", true);
        String payoutFrequency = extractStringField(requestBody, "payoutFrequency", true);

        // Extract optional fields
        String description = extractStringField(requestBody, "description", false);
        String profileImage = extractStringField(requestBody, "profileImage", false);
        Date contributionDate = extractDateField(requestBody, "contributionDate");
        Date payoutDate = extractDateField(requestBody, "payoutDate");
        
        @SuppressWarnings("unchecked")
        List<String> members = (List<String>) requestBody.getOrDefault("members", new ArrayList<>());

        return new CreateGroupRequest(
            adminId, name, minContribution, maxMembers, description, profileImage,
            visibility, contributionFrequency, contributionDate, payoutFrequency, 
            payoutDate, members
        );
    }

    private String extractStringField(Map<String, Object> request, String fieldName, boolean required) {
        String value = (String) request.get(fieldName);
        
        if (required && (value == null || value.trim().isEmpty())) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        
        return value != null ? value.trim() : null;
    }

    private Double extractDoubleField(Map<String, Object> request, String fieldName, boolean required) {
        Object value = request.get(fieldName);
        
        if (required && value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                return Double.parseDouble(value.toString());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format");
        }
    }

    private Integer extractIntegerField(Map<String, Object> request, String fieldName, boolean required) {
        Object value = request.get(fieldName);
        
        if (required && value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format");
        }
    }

    private Date extractDateField(Map<String, Object> request, String fieldName) {
        String dateStr = (String) request.get(fieldName);
        
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return isoFormatter.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format. Expected ISO format");
        }
    }

    // DTO class for Swagger documentation
    @Schema(description = "Request payload for creating a new group")
    public static class CreateGroupRequestDto {
        @Schema(description = "Group name", example = "Investment Club", required = true)
        public String name;
        
        @Schema(description = "Minimum contribution amount", example = "100.0", required = true)
        public Double minContribution;
        
        @Schema(description = "Maximum number of members", example = "10", required = true)
        public Integer maxMembers;
        
        @Schema(description = "Group description", example = "Monthly investment group")
        public String description;
        
        @Schema(description = "Profile image URL")
        public String profileImage;
        
        @Schema(description = "Group visibility", example = "Public", allowableValues = {"Public", "Private"}, required = true)
        public String visibility;
        
        @Schema(description = "Contribution frequency", example = "Monthly", allowableValues = {"Weekly", "Bi-weekly", "Monthly"}, required = true)
        public String contributionFrequency;
        
        @Schema(description = "Contribution date in ISO format", example = "2025-07-15T10:00:00.000Z")
        public String contributionDate;
        
        @Schema(description = "Payout frequency", example = "Monthly", allowableValues = {"Weekly", "Bi-weekly", "Monthly"}, required = true)
        public String payoutFrequency;
        
        @Schema(description = "Payout date in ISO format", example = "2025-07-30T10:00:00.000Z")
        public String payoutDate;
        
        @Schema(description = "List of initial member user IDs", example = "[\"user123\", \"user456\"]")
        public List<String> members;
    }
}