package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.*;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.*;

@RestController
@RequestMapping("/api/activity-logs")
@CrossOrigin(origins = "*")
public class ActivityLogController {
    
    @Autowired
    private ActivityLogService activityLogService;

    // Get activity logs by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ActivityLogResponseDto>> getActivityLogsByUser(
            @PathVariable UUID userId, 
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.findByUserId(userId, pageable);
        return ResponseEntity.ok(logs.map(ActivityLogResponseDto::fromEntity));
    }
    
    // Get activity logs by cycle
    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<Page<ActivityLogResponseDto>> getActivityLogsByCycle(
            @PathVariable UUID cycleId, 
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.findByCycleId(cycleId, pageable);
        return ResponseEntity.ok(logs.map(ActivityLogResponseDto::fromEntity));
    }
    
    // Get activity logs by entity
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<ActivityLogResponseDto>> getActivityLogsByEntity(
            @PathVariable ActivityLog.EntityType entityType,
            @PathVariable UUID entityId,
            Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return ResponseEntity.ok(logs.map(ActivityLogResponseDto::fromEntity));
    }
}
