package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.GroupCycleResponseDto;
import com.stockfellow.transactionservice.dto.CreateGroupCycleDto;
import com.stockfellow.transactionservice.dto.UpdateCycleStatusDto;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.service.GroupCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.*;
import io.swagger.v3.oas.annotations.Operation;


// 1. Group Cycle Controller - Handles cycle creation from external service
@RestController
@Tag(name = "Group Cycles", description = "Operations related to group contribution cycles")
@RequestMapping("/api/cycles")
@CrossOrigin(origins = "*")
public class GroupCycleController {
    
    @Autowired
    private GroupCycleService groupCycleService;
    
    // @Autowired
    // private ActivityLogService activityLogService;

    // Create cycle when group is created in external service
    @PostMapping
    @Operation(summary = "Create a new group cycle", 
               description = "Creates a new investment cycle for a group with specified parameters and timeline")
    public ResponseEntity<GroupCycleResponseDto> createCycle(@Valid @RequestBody CreateGroupCycleDto createDto) {
        GroupCycle cycle = groupCycleService.createGroupCycle(createDto);
        // activityLogService.logActivity(null, cycle.getCycleId(), ActivityLog.EntityType.GROUP_CYCLE, 
        //                              cycle.getCycleId(), "CYCLE_CREATED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(GroupCycleResponseDto.fromEntity(cycle));
    }
    
    @GetMapping("/{cycleId}")
        @Operation(summary = "Get cycle by ID", 
               description = "Retrieves a specific group cycle by its unique identifier")
    public ResponseEntity<GroupCycleResponseDto> getCycle(@PathVariable UUID cycleId) {
        GroupCycle cycle = groupCycleService.getCycleById(cycleId);
        return ResponseEntity.ok(GroupCycleResponseDto.fromEntity(cycle));
    }
    
    // Update cycle status (e.g., activate, complete)
    @PutMapping("/{cycleId}/status")
        @Operation(summary = "Update cycle status", 
               description = "Updates a specific group cycle's status using its unique identifier")
    public ResponseEntity<GroupCycleResponseDto> updateCycleStatus(
            @PathVariable UUID cycleId, 
            @RequestBody UpdateCycleStatusDto statusDto) {
        GroupCycle cycle = groupCycleService.updateCycleStatus(cycleId, statusDto.getStatus());
        return ResponseEntity.ok(GroupCycleResponseDto.fromEntity(cycle));
    }
    
    // Get cycles by group
    @GetMapping("/group/{groupId}")
        @Operation(summary = "Get cycles by group", 
               description = "Retrieves all cycles associated with a specific group")
    public ResponseEntity<List<GroupCycleResponseDto>> getCyclesByGroup(@PathVariable UUID groupId) {
        List<GroupCycle> cycles = groupCycleService.getCyclesByGroup(groupId);
        return ResponseEntity.ok(cycles.stream()
                               .map(GroupCycleResponseDto::fromEntity)
                               .toList());
    }
}