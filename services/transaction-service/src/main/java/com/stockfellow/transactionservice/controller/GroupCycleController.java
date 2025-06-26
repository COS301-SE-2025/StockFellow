package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class GroupCycleController {

    private final GroupCycleRepository groupCycleRepository;

    // Get all cycles

    @GetMapping
    public ResponseEntity<List<GroupCycle>> getAllCycles() {
        log.info("Getting all group cycles");
        List<GroupCycle> cycles = groupCycleRepository.findAll();
        return ResponseEntity.ok(cycles);
    }

    // Get cycle by ID

    @GetMapping("/{cycleId}")
    public ResponseEntity<GroupCycle> getCycle(@PathVariable UUID cycleId) {
        log.info("Getting cycle: {}", cycleId);
        return groupCycleRepository.findById(cycleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get cycles by group (using your existing method)

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<GroupCycle>> getCyclesByGroup(@PathVariable UUID groupId) {
        log.info("Getting cycles for group: {}", groupId);
        List<GroupCycle> cycles = groupCycleRepository.findByGroupIdOrderByCollectionDateDesc(groupId);
        return ResponseEntity.ok(cycles);
    }

    // Get cycles by status (using your existing method)

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GroupCycle>> getCyclesByStatus(@PathVariable String status) {
        log.info("Getting cycles with status: {}", status);
        List<GroupCycle> cycles = groupCycleRepository.findByStatus(status);
        return ResponseEntity.ok(cycles);
    }

    // Get next upcoming cycle for a group (using your existing method)

    @GetMapping("/group/{groupId}/next")
    public ResponseEntity<GroupCycle> getNextCycleForGroup(@PathVariable UUID groupId,
            @RequestParam(defaultValue = "PENDING") String status) {
        log.info("Getting next cycle for group: {} with status: {}", groupId, status);
        Optional<GroupCycle> cycle = groupCycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(groupId,
                status);
        return cycle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get next upcoming cycle regardless of group (using your existing method)

    @GetMapping("/upcoming")
    public ResponseEntity<GroupCycle> getNextUpcomingCycle(@RequestParam(defaultValue = "PENDING") String status) {
        log.info("Getting next upcoming cycle with status: {}", status);
        Optional<GroupCycle> cycle = groupCycleRepository
                .findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
                        status, LocalDate.now());
        return cycle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get cycle by group and month (using your existing method)

    @GetMapping("/group/{groupId}/month/{cycleMonth}")
    public ResponseEntity<GroupCycle> getCycleByGroupAndMonth(@PathVariable UUID groupId,
            @PathVariable String cycleMonth) {
        log.info("Getting cycle for group: {} in month: {}", groupId, cycleMonth);
        Optional<GroupCycle> cycle = groupCycleRepository.findByGroupIdAndCycleMonth(groupId, cycleMonth);
        return cycle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get earliest cycles by group (using your existing method)

    @GetMapping("/group/{groupId}/earliest")
    public ResponseEntity<List<GroupCycle>> getEarliestCyclesForGroup(@PathVariable UUID groupId) {
        log.info("Getting earliest cycles for group: {}", groupId);
        List<GroupCycle> cycles = groupCycleRepository.findFirstByGroupIdOrderByCollectionDateAsc(groupId);
        return ResponseEntity.ok(cycles);
    }
}
