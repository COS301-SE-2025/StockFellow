package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.repository.MandateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/mandates")
@RequiredArgsConstructor
public class MandateController {

    private final MandateRepository mandateRepository;

    // Get all mandates

    @GetMapping
    public ResponseEntity<List<Mandate>> getAllMandates() {
        log.info("Getting all mandates");
        List<Mandate> mandates = mandateRepository.findAll();
        return ResponseEntity.ok(mandates);
    }

    // Get mandate by ID

    @GetMapping("/{mandateId}")
    public ResponseEntity<Mandate> getMandate(@PathVariable UUID mandateId) {
        log.info("Getting mandate: {}", mandateId);
        return mandateRepository.findById(mandateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get mandates by group

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Mandate>> getMandatesByGroup(@PathVariable UUID groupId) {
        log.info("Getting mandates for group: {}", groupId);
        List<Mandate> mandates = mandateRepository.findByGroupId(groupId);
        return ResponseEntity.ok(mandates);
    }

    // Get mandates by status

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Mandate>> getMandatesByStatus(@PathVariable String status) {
        log.info("Getting mandates with status: {}", status);
        List<Mandate> mandates = mandateRepository.findByStatus(status);
        return ResponseEntity.ok(mandates);
    }

    // Get active mandates for a group

    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<List<Mandate>> getActiveMandatesForGroup(@PathVariable UUID groupId) {
        log.info("Getting active mandates for group: {}", groupId);
        List<Mandate> mandates = mandateRepository.findByGroupIdAndStatus(groupId, "ACTIVE");
        return ResponseEntity.ok(mandates);
    }
}
