package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.CreateMandateRequest;
import com.stockfellow.transactionservice.dto.MandateResponse;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.service.MandateService;

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
@Tag(name = "Mandates", description = "Operations related to user mandates for group participation")
@RequestMapping("/api/mandates")
public class MandateController {

    private static final Logger logger = LoggerFactory.getLogger(MandateController.class);
    private final MandateService mandateService;

    public MandateController(MandateService mandateService) {
        this.mandateService = mandateService;
    }

    @PostMapping
    @Operation(summary = "Create a new mandate", 
               description = "Creates a mandate between a user and a group, allowing the user to participate in group investment cycles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Mandate created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = MandateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Business logic conflict (e.g., mandate already exists)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MandateResponse> createMandate(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Mandate creation request", 
                required = true,
                content = @Content(schema = @Schema(implementation = CreateMandateRequest.class))
            ) CreateMandateRequest request) {
        
        logger.info("Received request to create mandate for user " + request.getPayerUserId() + " in group " + request.getGroupId());
        
        try {
            Mandate mandate = mandateService.createMandate(request);
            MandateResponse response = MandateResponse.from(mandate);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.warn("Business logic error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.warn("Unexpected error creating mandate: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all mandates", 
               description = "Retrieves a list of all mandates in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of mandates retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = MandateResponse.class)))
    })
    public ResponseEntity<List<MandateResponse>> getAllMandates() {
        logger.info("Getting all mandates");
        List<MandateResponse> mandates = mandateService.getAllMandates();
        return ResponseEntity.ok(mandates);
    }

    @GetMapping("/{mandateId}")
    @Operation(summary = "Get mandate by ID", 
               description = "Retrieves a specific mandate by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mandate retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = MandateResponse.class))),
        @ApiResponse(responseCode = "404", description = "Mandate not found")
    })
    public ResponseEntity<MandateResponse> getMandateById(
            @Parameter(description = "The unique identifier of the mandate", required = true)
            @PathVariable UUID mandateId) {
        logger.info("Getting mandate by ID: " + mandateId);
        try {
            Mandate mandate = mandateService.getMandateById(mandateId);
            MandateResponse response = MandateResponse.from(mandate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{mandateId}/deactivate")
    @Operation(summary = "Deactivate a mandate", 
               description = "Deactivates an existing mandate, preventing future transactions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mandate deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Mandate not found")
    })
    public ResponseEntity<Void> deactivateMandate(
            @Parameter(description = "The unique identifier of the mandate to deactivate", required = true)
            @PathVariable UUID mandateId) {
        logger.info("Deactivating mandate: {}", mandateId);
        try {
            mandateService.deactivateMandate(mandateId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Mandate not found for deactivation: {}", mandateId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get mandates by group", 
               description = "Retrieves all mandates associated with a specific group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group mandates retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = MandateResponse.class)))
    })
    public ResponseEntity<List<MandateResponse>> getMandatesByGroup(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable UUID groupId) {
        logger.info("Getting mandates for group: " + groupId);
        List<MandateResponse> mandates = mandateService.getMandatesByGroup(groupId);
        return ResponseEntity.ok(mandates);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get mandates by status", 
               description = "Retrieves all mandates with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mandates with specified status retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = MandateResponse.class)))
    })
    public ResponseEntity<List<MandateResponse>> getMandatesByStatus(
            @Parameter(description = "The status of the mandates to retrieve (ACTIVE, INACTIVE, PENDING, EXPIRED)", 
                      required = true)
            @PathVariable String status) {
        logger.info("Getting mandates with status: " + status);
        List<MandateResponse> mandates = mandateService.getMandatesByStatus(status);
        return ResponseEntity.ok(mandates);
    }

    @GetMapping("/group/{groupId}/active")
    @Operation(summary = "Get active mandates for a group", 
               description = "Retrieves all active mandates for a specific group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active mandates for the group retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = MandateResponse.class)))
    })
    public ResponseEntity<List<MandateResponse>> getActiveMandatesForGroup(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable UUID groupId) {
        logger.info("Getting active mandates for group: " + groupId);
        List<MandateResponse> mandates = mandateService.getActiveMandatesByGroup(groupId);
        return ResponseEntity.ok(mandates);
    }
}