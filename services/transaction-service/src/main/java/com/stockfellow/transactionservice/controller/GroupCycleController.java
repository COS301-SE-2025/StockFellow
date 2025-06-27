package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.CycleResponse;
import com.stockfellow.transactionservice.dto.CreateCycleRequest;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.service.CycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cycles")
public class GroupCycleController {

    private static final Logger logger = LoggerFactory.getLogger(GroupCycleController.class);
    private final CycleService cycleService;

    public GroupCycleController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    // Create group cycle - Complex operation, needs try-catch for different error types
    @PostMapping
    public ResponseEntity<CycleResponse> createGroupCycle(@Valid @RequestBody CreateCycleRequest request) {
        logger.info("Received request to create group cycle for group {}", request.getGroupId());
        
        try {
            GroupCycle cycle = cycleService.createGroupCycle(request);
            CycleResponse response = CycleResponse.from(cycle);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.warn("Business logic error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Unexpected error creating group cycle: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all cycles - List operation, no try-catch needed
    @GetMapping
    public ResponseEntity<List<CycleResponse>> getAllCycles() {
        logger.info("Getting all group cycles");
        List<CycleResponse> cycles = cycleService.getAllCycles();
        return ResponseEntity.ok(cycles);
    }

    // Get cycle by ID - Single item, needs try-catch for not found
    @GetMapping("/{cycleId}")
    public ResponseEntity<CycleResponse> getCycle(@PathVariable UUID cycleId) {
        logger.info("Getting group cycle by ID: {}", cycleId);
        try {
            GroupCycle cycle = cycleService.getCycleById(cycleId);
            CycleResponse response = CycleResponse.from(cycle);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Cycle not found: {}", cycleId);
            return ResponseEntity.notFound().build();
        }
    }

    // Get cycles by group - List operation, no try-catch needed
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<CycleResponse>> getCyclesByGroup(@PathVariable UUID groupId) {
        logger.info("Getting cycles for group: {}", groupId);
        List<CycleResponse> cycles = cycleService.getCyclesByGroup(groupId);
        return ResponseEntity.ok(cycles);
    }

    // Get cycles by status - List operation, no try-catch needed
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CycleResponse>> getCyclesByStatus(@PathVariable String status) {
        logger.info("Getting cycles with status: {}", status);
        List<CycleResponse> cycles = cycleService.getCyclesByStatus(status);
        return ResponseEntity.ok(cycles);
    }

    // Get next upcoming cycle for a group - Single item, needs try-catch for not found
    @GetMapping("/group/{groupId}/next")
    public ResponseEntity<CycleResponse> getNextCycleForGroup(@PathVariable UUID groupId,
            @RequestParam(defaultValue = "PENDING") String status) {
        logger.info("Getting next cycle for group: {} with status: {}", groupId, status);
        try {
            GroupCycle cycle = cycleService.getNextCycleForGroup(groupId, status);
            CycleResponse response = CycleResponse.from(cycle);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("No upcoming cycle found for group: {} with status: {}", groupId, status);
            return ResponseEntity.notFound().build();
        }
    }

    // Get next upcoming cycle regardless of group - Single item, needs try-catch for not found
    @GetMapping("/upcoming")
    public ResponseEntity<CycleResponse> getNextUpcomingCycle(@RequestParam(defaultValue = "PENDING") String status) {
        logger.info("Getting next upcoming cycle with status: {}", status);
        try {
            GroupCycle cycle = cycleService.getNextUpcomingCycle(status);
            CycleResponse response = CycleResponse.from(cycle);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("No upcoming cycle found with status: {}", status);
            return ResponseEntity.notFound().build();
        }
    }

    // Get cycle by group and month - Single item, needs try-catch for not found
    @GetMapping("/group/{groupId}/month/{cycleMonth}")
    public ResponseEntity<CycleResponse> getCycleByGroupAndMonth(@PathVariable UUID groupId,
            @PathVariable String cycleMonth) {
        logger.info("Getting cycle for group: {} in month: {}", groupId, cycleMonth);
        try {
            GroupCycle cycle = cycleService.getCycleByGroupAndMonth(groupId, cycleMonth);
            CycleResponse response = CycleResponse.from(cycle);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("No cycle found for group: {} in month: {}", groupId, cycleMonth);
            return ResponseEntity.notFound().build();
        }
    }

    // Get earliest cycles by group - List operation, no try-catch needed
    @GetMapping("/group/{groupId}/earliest")
    public ResponseEntity<List<CycleResponse>> getEarliestCyclesForGroup(@PathVariable UUID groupId) {
        logger.info("Getting earliest cycles for group: {}", groupId);
        List<CycleResponse> cycles = cycleService.getEarliestCyclesForGroup(groupId);
        return ResponseEntity.ok(cycles);
    }
}