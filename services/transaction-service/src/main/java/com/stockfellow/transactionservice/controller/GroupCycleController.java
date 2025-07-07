package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.CycleResponse;
import com.stockfellow.transactionservice.dto.CreateCycleRequest;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.service.CycleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Group Cycles", description = "Operations related to group contribution cycles")
@RequestMapping("/api/cycles")
public class GroupCycleController {

    private static final Logger logger = LoggerFactory.getLogger(GroupCycleController.class);
    private final CycleService cycleService;

    public GroupCycleController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @PostMapping
    @Operation(summary = "Create a new group cycle", 
               description = "Creates a new investment cycle for a group with specified parameters and timeline")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Group cycle created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Business logic conflict (e.g., cycle already exists for the period)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CycleResponse> createGroupCycle(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Group cycle creation request", 
                required = true,
                content = @Content(schema = @Schema(implementation = CreateCycleRequest.class))
            ) CreateCycleRequest request) {
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

    @GetMapping
    @Operation(summary = "Get all cycles", 
               description = "Retrieves a list of all group cycles in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of cycles retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class)))
    })
    public ResponseEntity<List<CycleResponse>> getAllCycles() {
        logger.info("Getting all group cycles");
        List<CycleResponse> cycles = cycleService.getAllCycles();
        return ResponseEntity.ok(cycles);
    }

    @GetMapping("/{cycleId}")
    @Operation(summary = "Get cycle by ID", 
               description = "Retrieves a specific group cycle by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cycle retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cycle not found")
    })
    public ResponseEntity<CycleResponse> getCycle(
            @Parameter(description = "The unique identifier of the cycle", required = true)
            @PathVariable UUID cycleId) {
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

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get cycles by group", 
               description = "Retrieves all cycles associated with a specific group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group cycles retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class)))
    })
    public ResponseEntity<List<CycleResponse>> getCyclesByGroup(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable UUID groupId) {
        logger.info("Getting cycles for group: {}", groupId);
        List<CycleResponse> cycles = cycleService.getCyclesByGroup(groupId);
        return ResponseEntity.ok(cycles);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get cycles by status", 
               description = "Retrieves all cycles with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cycles with specified status retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class)))
    })
    public ResponseEntity<List<CycleResponse>> getCyclesByStatus(
            @Parameter(description = "The status of the cycles to retrieve (PENDING, ACTIVE, COMPLETED, CANCELLED)", 
                      required = true)
            @PathVariable String status) {
        logger.info("Getting cycles with status: {}", status);
        List<CycleResponse> cycles = cycleService.getCyclesByStatus(status);
        return ResponseEntity.ok(cycles);
    }

    @GetMapping("/group/{groupId}/next")
    @Operation(summary = "Get next upcoming cycle for a group", 
               description = "Retrieves the next upcoming cycle for a specific group, optionally filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Next cycle for the group retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class))),
        @ApiResponse(responseCode = "404", description = "No upcoming cycle found for the group")
    })
    public ResponseEntity<CycleResponse> getNextCycleForGroup(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable UUID groupId,
            @Parameter(description = "The status of the cycle to retrieve", example = "PENDING")
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

    @GetMapping("/upcoming")
    @Operation(summary = "Get next upcoming cycle", 
               description = "Retrieves the next upcoming cycle across all groups, optionally filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Next upcoming cycle retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class))),
        @ApiResponse(responseCode = "404", description = "No upcoming cycle found")
    })
    public ResponseEntity<CycleResponse> getNextUpcomingCycle(
            @Parameter(description = "The status of the cycle to retrieve", example = "PENDING")
            @RequestParam(defaultValue = "PENDING") String status) {
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

    @GetMapping("/group/{groupId}/month/{cycleMonth}")
    @Operation(summary = "Get cycle by group and month", 
               description = "Retrieves a cycle for a specific group in a specific month (YYYY-MM format)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cycle for the specified group and month retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class))),
        @ApiResponse(responseCode = "404", description = "No cycle found for the specified group and month")
    })
    public ResponseEntity<CycleResponse> getCycleByGroupAndMonth(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable UUID groupId,
            @Parameter(description = "The cycle month in YYYY-MM format", required = true, example = "2024-01")
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

    @GetMapping("/group/{groupId}/earliest")
    @Operation(summary = "Get earliest cycles for a group", 
               description = "Retrieves the earliest cycles for a specific group, useful for historical data analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Earliest cycles for the group retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CycleResponse.class)))
    })
    public ResponseEntity<List<CycleResponse>> getEarliestCyclesForGroup(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable UUID groupId) {
        logger.info("Getting earliest cycles for group: {}", groupId);
        List<CycleResponse> cycles = cycleService.getEarliestCyclesForGroup(groupId);
        return ResponseEntity.ok(cycles);
    }
}