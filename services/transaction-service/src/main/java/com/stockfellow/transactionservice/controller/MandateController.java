package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.CreateMandateRequest;
import com.stockfellow.transactionservice.dto.MandateResponse;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.service.MandateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mandates")
public class MandateController {

    private static final Logger logger = LoggerFactory.getLogger(MandateController.class);
    private final MandateService mandateService;

    public MandateController(MandateService mandateService) {
        this.mandateService = mandateService;
    }


    //Get all 
    //get by id
    //get by group
    //get by status
    //get by active for specific group


    // Create Mandate between user and group

    @PostMapping
    public ResponseEntity<MandateResponse> createMandate(@Valid @RequestBody CreateMandateRequest request) {
        
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

    //Get all mandates

    @GetMapping
    public ResponseEntity<List<MandateResponse>> getAllMandates() {
        logger.info("Getting all mandates");
        List<MandateResponse> mandates = mandateService.getAllMandates();
        return ResponseEntity.ok(mandates);
    }

    // Get mandate by Id
    
    @GetMapping("/{mandateId}")
    public ResponseEntity<MandateResponse> getMandateById(@PathVariable UUID mandateId) {
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
    public ResponseEntity<Void> deactivateMandate(@PathVariable UUID mandateId) {
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
    public ResponseEntity<List<MandateResponse>> getMandatesByGroup(@PathVariable UUID groupId) {
        logger.info("Getting mandates for group: " + groupId);
        List<MandateResponse> mandates = mandateService.getMandatesByGroup(groupId);
        return ResponseEntity.ok(mandates);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MandateResponse>> getMandatesByStatus(@PathVariable String status) {
        logger.info("Getting mandates with status: " + status);
        List<MandateResponse> mandates = mandateService.getMandatesByStatus(status);
        return ResponseEntity.ok(mandates);
    }

    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<List<MandateResponse>> getActiveMandatesForGroup(@PathVariable UUID groupId) {
        logger.info("Getting active mandates for group: " + groupId);
        List<MandateResponse> mandates = mandateService.getActiveMandatesByGroup(groupId);
        return ResponseEntity.ok(mandates);
    }
}