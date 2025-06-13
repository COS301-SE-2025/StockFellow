package com.stockfellow.groupservice.controller;

import com.stockfellow.groupservice.command.CreateGroupCommand;
import com.stockfellow.groupservice.command.JoinGroupCommand;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.service.ReadModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {
    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);
    private final CreateGroupCommand createGroupCommand;
    private final JoinGroupCommand joinGroupCommand;
    private final ReadModelService readModelService;

    public GroupsController(CreateGroupCommand createGroupCommand,
                            JoinGroupCommand joinGroupCommand,
                            ReadModelService readModelService) {
        this.createGroupCommand = createGroupCommand;
        this.joinGroupCommand = joinGroupCommand;
        this.readModelService = readModelService;
    }

    @GetMapping
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Group Service");
        response.put("version", "1.0.0");
        response.put("endpoints", Arrays.asList(
                "POST /api/groups/create - Create a new group",
                "GET /api/groups/user - Get groups for authenticated user",
                "POST /api/groups/{groupId}/join - Join a group"
        ));
        return response;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request) {
        try {
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
            List<String> memberIds = (List<String>) request.getOrDefault("memberIds", new ArrayList<>());

            if (name == null || minContributionObj == null || maxMembersObj == null || visibility == null ||
                    contributionFrequency == null || payoutFrequency == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            Double minContribution = minContributionObj instanceof Number ?
                    ((Number) minContributionObj).doubleValue() :
                    Double.parseDouble(minContributionObj.toString());
            Integer maxMembers = maxMembersObj instanceof Number ?
                    ((Number) maxMembersObj).intValue() :
                    Integer.parseInt(maxMembersObj.toString());

            if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(contributionFrequency)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid contribution frequency"));
            }
            if (!Arrays.asList("Monthly", "Bi-weekly", "Weekly").contains(payoutFrequency)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid payout frequency"));
            }
            if (!Arrays.asList("Private", "Public").contains(visibility)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid visibility"));
            }
            if (minContribution <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid minimum contribution"));
            }
            if (maxMembers <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid maximum number of members"));
            }
            if (memberIds.size() > maxMembers) {
                return ResponseEntity.badRequest().body(Map.of("error", "Number of memberIds cannot exceed maxMembers"));
            }
            Date contributionDate = contributionDateStr != null && !contributionDateStr.isEmpty() ?
                    new Date(contributionDateStr) : null;
            Date payoutDate = payoutDateStr != null && !payoutDateStr.isEmpty() ?
                    new Date(payoutDateStr) : null;

            String groupId = "group_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
            String adminId = "e20f93e2-d283-4100-a5fa-92c61d85b4f4"; // Placeholder, replace with JWT subject

            String eventId = createGroupCommand.execute(groupId, adminId, name, minContribution, maxMembers,
                    description, profileImage, visibility, contributionFrequency, contributionDate,
                    payoutFrequency, payoutDate, memberIds);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Group created successfully");
            response.put("groupId", groupId);
            response.put("eventId", eventId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Group creation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserGroups() {
        try {
            String userId = "userId1"; // Placeholder, replace with JWT subject
            List<Group> groups = readModelService.getUserGroups(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error fetching user groups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable String groupId) {
        try {
            String userId = "3372d535-05a1-4189-b6ff-a2291cb1145c"; // Placeholder, replace with JWT subject
            readModelService.getGroup(groupId).orElseThrow(() -> new IllegalStateException("Group not found"));
            String eventId = joinGroupCommand.execute(groupId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully joined group");
            response.put("groupId", groupId);
            response.put("eventId", eventId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.error("Error joining group: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}
