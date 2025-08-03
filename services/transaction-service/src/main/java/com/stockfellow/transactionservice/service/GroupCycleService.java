package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateGroupCycleDto;
import com.stockfellow.transactionservice.dto.GroupCycleResponseDto;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.model.GroupCycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupCycleService {
    private static final Logger logger = LoggerFactory.getLogger(GroupCycleService.class);
    private final GroupCycleRepository groupCycleRepository;

    public GroupCycleService(GroupCycleRepository groupCycleRepository) {
        this.groupCycleRepository = groupCycleRepository;
    }

    // @Transactional
    // public GroupCycle createGroupCycle(CreateCycleRequest request) {
    //     logger.info("Creating Group Cycle for group {}", request.getGroupId());

    //     validateCreateCycleRequest(request);

    //     // Check if cycle exists for this group and month
    //     if (cycleRepository.findByGroupIdAndCycleMonth(request.getGroupId(), request.getCycleMonth()).isPresent()) {
    //         throw new IllegalStateException("Cycle already exists for group " + request.getGroupId() + " in month " + request.getCycleMonth());
    //     }

    //     GroupCycle cycle = new GroupCycle();
    //     cycle.setGroupId(request.getGroupId());
    //     cycle.setCycleMonth(request.getCycleMonth());
    //     cycle.setRecipientUserId(request.getRecipientUserId());
    //     cycle.setRecipientPaymentMethodId(request.getRecipientPaymentMethodId());
    //     cycle.setContributionAmount(request.getContributionAmount());
    //     cycle.setCollectionDate(request.getCollectionDate());
    //     cycle.setTotalExpectedAmount(request.getTotalExpectedAmount());
    //     cycle.setStatus("PENDING"); // Set default status
    //     cycle.setSuccessfulPayments(0); // Initialize counters
    //     cycle.setFailedPayments(0);

    //     GroupCycle savedCycle = cycleRepository.save(cycle);

    //     logger.info("Successfully created cycle with ID: {}", savedCycle.getCycleId());
    //     return savedCycle;
    // }

    // public GroupCycle getCycleById(UUID cycleId) {
    //     logger.info("Getting cycle by ID: {}", cycleId);
    //     return cycleRepository.findById(cycleId)
    //         .orElseThrow(() -> new IllegalArgumentException("Cycle not found with ID: " + cycleId));
    // }

    // public List<CycleResponse> getAllCycles() {
    //     logger.info("Getting all cycles");
    //     List<GroupCycle> cycles = cycleRepository.findAll();
    //     return cycles.stream()
    //             .map(CycleResponse::from)
    //             .collect(Collectors.toList());
    // }

    // public List<CycleResponse> getCyclesByGroup(UUID groupId) {
    //     logger.info("Getting cycles for group: {}", groupId);
    //     List<GroupCycle> cycles = cycleRepository.findByGroupIdOrderByCollectionDateDesc(groupId);
    //     return cycles.stream()
    //             .map(CycleResponse::from)
    //             .collect(Collectors.toList());
    // }

    // public List<CycleResponse> getCyclesByStatus(String status) {
    //     logger.info("Getting cycles with status: {}", status);
    //     List<GroupCycle> cycles = cycleRepository.findByStatus(status);
    //     return cycles.stream()
    //             .map(CycleResponse::from)
    //             .collect(Collectors.toList());
    // }

    // public GroupCycle getNextCycleForGroup(UUID groupId, String status) {
    //     logger.info("Getting next cycle for group: {} with status: {}", groupId, status);
    //     Optional<GroupCycle> cycle = cycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(groupId, status);
    //     return cycle.orElseThrow(() -> new IllegalArgumentException("No upcoming cycle found for group: " + groupId));
    // }

    // public GroupCycle getNextUpcomingCycle(String status) {
    //     logger.info("Getting next upcoming cycle with status: {}", status);
    //     Optional<GroupCycle> cycle = cycleRepository
    //             .findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(status, LocalDate.now());
    //     return cycle.orElseThrow(() -> new IllegalArgumentException("No upcoming cycle found with status: " + status));
    // }

    // public GroupCycle getCycleByGroupAndMonth(UUID groupId, String cycleMonth) {
    //     logger.info("Getting cycle for group: {} in month: {}", groupId, cycleMonth);
    //     Optional<GroupCycle> cycle = cycleRepository.findByGroupIdAndCycleMonth(groupId, cycleMonth);
    //     return cycle.orElseThrow(() -> new IllegalArgumentException("No cycle found for group: " + groupId + " in month: " + cycleMonth));
    // }

    // public List<CycleResponse> getEarliestCyclesForGroup(UUID groupId) {
    //     logger.info("Getting earliest cycles for group: {}", groupId);
    //     List<GroupCycle> cycles = cycleRepository.findFirstByGroupIdOrderByCollectionDateAsc(groupId);
    //     return cycles.stream()
    //             .map(CycleResponse::from)
    //             .collect(Collectors.toList());
    // }

    // @Transactional
    // public GroupCycle updateCycleStatus(UUID cycleId, String status) {
    //     logger.info("Updating cycle {} to status: {}", cycleId, status);
    //     GroupCycle cycle = getCycleById(cycleId);
    //     cycle.setStatus(status);
    //     return cycleRepository.save(cycle);
    // }

    // @Transactional
    // public GroupCycle updatePaymentCounts(UUID cycleId, int successfulPayments, int failedPayments) {
    //     logger.info("Updating payment counts for cycle {}: successful={}, failed={}", cycleId, successfulPayments, failedPayments);
    //     GroupCycle cycle = getCycleById(cycleId);
    //     cycle.setSuccessfulPayments(successfulPayments);
    //     cycle.setFailedPayments(failedPayments);
    //     return cycleRepository.save(cycle);
    // }

    // private void validateCreateCycleRequest(CreateCycleRequest request) {
    //     if (request.getGroupId() == null) {
    //         throw new IllegalArgumentException("Group ID cannot be null");
    //     }
    //     if (request.getCycleMonth() == null || request.getCycleMonth().trim().isEmpty()) {
    //         throw new IllegalArgumentException("Cycle Month cannot be null or empty");
    //     }
    //     if (request.getRecipientUserId() == null) {
    //         throw new IllegalArgumentException("Recipient ID cannot be null");
    //     }
    //     if (request.getRecipientPaymentMethodId() == null) {
    //         throw new IllegalArgumentException("Recipient Payment method ID cannot be null");
    //     }
    //     if (request.getContributionAmount() == null || request.getContributionAmount().compareTo(BigDecimal.ZERO) <= 0) {
    //         throw new IllegalArgumentException("Contribution amount must be greater than 0");
    //     }
    //     if (request.getCollectionDate() == null) {
    //         throw new IllegalArgumentException("Collection Date cannot be null");
    //     }
    //     if (request.getTotalExpectedAmount() == null || request.getTotalExpectedAmount().compareTo(BigDecimal.ZERO) <= 0) {
    //         throw new IllegalArgumentException("Total expected amount must be greater than 0");
    //     }
    //     // Validate collection date is in the future
    //     if (request.getCollectionDate().isBefore(LocalDate.now())) {
    //         throw new IllegalArgumentException("Collection date must be in the future");
    //     }
    // }
}