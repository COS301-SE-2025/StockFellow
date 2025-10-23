package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.ActivityLog;
import com.stockfellow.transactionservice.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


@RestController
@Tag(
    name = "Activity Log Controller", 
    description = "Get info about events related to: Users, Transactions, Transfers, GroupCycles, Payment details"
)
@RequestMapping("/api/activity-logs")
@CrossOrigin(origins = "*")
public class ActivityLogController {
    
    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get User logs",
                description = "Get activity logs by user")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByUser(
            @PathVariable UUID userId, 
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.getLogsByUser(userId, pageable);
        return ResponseEntity.ok(logs.map(ActivityLog::fromEntity));
    }
    
    @GetMapping("/cycle/{cycleId}")
    @Operation(summary = "Get Group Cycle logs",
                description = "Get activity logs by cycle")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByCycle(
            @PathVariable UUID cycleId, 
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.getLogsByCycle(cycleId, pageable);
        return ResponseEntity.ok(logs.map(ActivityLog::fromEntity));
    }

    @GetMapping("/entity/{entityType}")
    @Operation(summary = "Get Entity logs",
                description = "Get activity logs by entity")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByEntity(
            @PathVariable ActivityLog.EntityType entityType,
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.getLogsByEntity(entityType, pageable);
        return ResponseEntity.ok(logs.map(ActivityLog::fromEntity));
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get specific entitys logs",
                description = "Get activity logs by entity id")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByEntityId(
            @PathVariable ActivityLog.EntityType entityType,
            @PathVariable UUID entityId,
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.getLogsByEntityId(entityType, entityId, pageable);
        return ResponseEntity.ok(logs.map(ActivityLog::fromEntity));
    }
}
