package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateGroupCycleDto;
import com.stockfellow.transactionservice.dto.GroupCycleResponseDto;
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

    private final GroupCycleRepository groupCycleRepository;
    private static final Logger logger = LoggerFactory.getLogger(GroupCycleService.class);

    public GroupCycleService(GroupCycleRepository groupCycleRepository) {
        this.groupCycleRepository = groupCycleRepository;
    }

    @Transactional
    public GroupCycle createGroupCycle(CreateGroupCycleDto request) {
        logger.info("Creating Group Cycle for group {}", request.getGroupId());

        validateCreateCycleRequest(request);

        // Check if cycle exists for this group and month
        if (groupCycleRepository.findByGroupIdAndCyclePeriod(request.getGroupId(), request.getCyclePeriod()).isPresent()) {
            throw new IllegalStateException("Cycle already exists for group " + request.getGroupId() + " in period " + request.getCyclePeriod());
        }

        GroupCycle cycle = new GroupCycle(
            request.getGroupId(),
            request.getCyclePeriod(),
            request.getRecipientUserId(),
            request.getContributionAmount(),
            request.getExpectedTotal(),
            request.getCollectionStartDate(),
            request.getCollectionEndDate()
        );
        
//        cycle.setStatus("pending"); // Set default status

        GroupCycle savedCycle = groupCycleRepository.save(cycle);

        logger.info("Successfully created cycle with ID: {}", savedCycle.getCycleId());
        return savedCycle;
    }

    public GroupCycle getCycleById(UUID cycleId) {
        logger.info("Getting cycle by ID: {}", cycleId);
        return groupCycleRepository.findById(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Cycle not found with ID: " + cycleId));
    }

    public List<GroupCycleResponseDto> getAllCycles() {
        logger.info("Getting all cycles");
        List<GroupCycle> cycles = groupCycleRepository.findAll();
        return cycles.stream()
                .map(GroupCycleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GroupCycle> getCyclesByGroup(UUID groupId) {
        logger.info("Getting cycles for group: {}", groupId);
        List<GroupCycle> cycles = groupCycleRepository.findByGroupIdOrderByCollectionStartDateDesc(groupId);
        return cycles;
    }

    public List<GroupCycleResponseDto> getCyclesByStatus(String status) {
        logger.info("Getting cycles with status: {}", status);
        List<GroupCycle> cycles = groupCycleRepository.findByStatus(status);
        return cycles.stream()
                .map(GroupCycleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public GroupCycle getNextCycleForGroup(UUID groupId, String status) {
        logger.info("Getting next cycle for group: {} with status: {}", groupId, status);
        Optional<GroupCycle> cycle = groupCycleRepository.findFirstByGroupIdAndStatusOrderByCollectionStartDateAsc(groupId, status);
        return cycle.orElseThrow(() -> new IllegalArgumentException("No upcoming cycle found for group: " + groupId));
    }

    public GroupCycle getNextUpcomingCycle(String status) {
        logger.info("Getting next upcoming cycle with status: {}", status);
        Optional<GroupCycle> cycle = groupCycleRepository.findFirstByStatusAndCollectionStartDateGreaterThanEqualOrderByCollectionStartDateAsc(status, LocalDate.now());
        return cycle.orElseThrow(() -> new IllegalArgumentException("No upcoming cycle found with status: " + status));
    }

    public GroupCycle getCycleByGroupAndMonth(UUID groupId, String cycleMonth) {
        logger.info("Getting cycle for group: {} in month: {}", groupId, cycleMonth);
        Optional<GroupCycle> cycle = groupCycleRepository.findByGroupIdAndCyclePeriod(groupId, cycleMonth);
        return cycle.orElseThrow(() -> new IllegalArgumentException("No cycle found for group: " + groupId + " in month: " + cycleMonth));
    }

    public List<GroupCycleResponseDto> getEarliestCyclesForGroup(UUID groupId) {
        logger.info("Getting earliest cycles for group: {}", groupId);
        Optional<GroupCycle> cycles = groupCycleRepository.findFirstByGroupIdOrderByCollectionStartDateAsc(groupId);
        return cycles.stream()
                .map(GroupCycleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupCycle updateCycleStatus(UUID cycleId, String status) {
        logger.info("Updating cycle {} to status: {}", cycleId, status);
        GroupCycle cycle = getCycleById(cycleId);
        cycle.setStatus(status);
        return groupCycleRepository.save(cycle);
    }

    // @Transactional
    // public GroupCycle updatePaymentCounts(UUID cycleId, int successfulPayments, int failedPayments) {
    //     logger.info("Updating payment counts for cycle {}: successful={}, failed={}", cycleId, successfulPayments, failedPayments);
    //     GroupCycle cycle = getCycleById(cycleId);
    //     cycle.setSuccessfulPayments(successfulPayments);
    //     cycle.setFailedPayments(failedPayments);
    //     return groupCycleRepository.save(cycle);
    // }

    //TODO: is this really necesary cant this be handled by DTO?
    private void validateCreateCycleRequest(CreateGroupCycleDto request) {
        if (request.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        if (request.getCyclePeriod() == null || request.getCyclePeriod().trim().isEmpty()) {
            throw new IllegalArgumentException("Cycle Period cannot be null or empty");
        }
        if (request.getRecipientUserId() == null) {
            throw new IllegalArgumentException("Recipient ID cannot be null");
        }
        if (request.getRecipientUserId() == null) {
            throw new IllegalArgumentException("Recipient User ID cannot be null");
        }
        if (request.getContributionAmount() == null || request.getContributionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than 0");
        }
        if (request.getCollectionStartDate() == null) {
            throw new IllegalArgumentException("Collection Start Date cannot be null");
        }
        if (request.getExpectedTotal() == null || request.getExpectedTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total expected amount must be greater than 0");
        }
        // Validate collection date is in the future
        if (request.getCollectionEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Collection end date must be in the future");
        }
    }
}