package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.repository.RotationRepository;
import com.stockfellow.transactionservice.dto.CreateGroupCycleDto;
import com.stockfellow.transactionservice.dto.CreateRotationDto;
import com.stockfellow.transactionservice.model.Rotation;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.service.GroupCycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.attribute.GroupPrincipal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RotationService {


    private final RotationRepository rotationRepository;
    private final GroupCycleService groupCycleService;
    private static final Logger logger = LoggerFactory.getLogger(RotationService.class);

    public RotationService(RotationRepository rotationRepository, GroupCycleService groupCycleService) {
        this.rotationRepository = rotationRepository;
        this.groupCycleService = groupCycleService;
    }

    @Transactional
    public Rotation createRotation(CreateRotationDto request) {
        logger.info("Creating Rotation for group {}", request.getGroupId());

        validateCreateRotationRequest(request);

        // Check if rotation exists for this group and month
        // if (rotationRepository.findByGroupId(request.getGroupId())) {
        //     throw new IllegalStateException("Rotation exists for group " + request.getGroupId());
        // }

        Rotation rotation = new Rotation(
            request.getGroupId(),
            request.getAmount(),
            request.getMemberIds(),
            request.getCollectionDate(),
            request.getPayoutDate(),
            request.getFrequency()
        );

        Rotation savedRotation = rotationRepository.save(rotation);
        logger.info("Successfully created rotation with ID: {}", savedRotation.getId());

        try {
            GroupCycle cycle = createGroupCycle(savedRotation);
            logger.info("Successfully created initial group cycle with ID: {}", cycle.getCycleId());
        } catch (Exception e) {
            logger.error("Failed to create group cycle for rotation {}: {}", 
                        savedRotation.getId(), e.getMessage());

            throw new RuntimeException("Failed to create group cycle", e);
        }
        return savedRotation;
    }

    private GroupCycle createGroupCycle(Rotation rotation) {
        // Calculate cycle period (e.g., "2025-10" for October 2025)
        String cyclePeriod = rotation.getCollectionDate().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
        );
        
        // Get current recipient based on rotation position
        UUID recipientUserId = rotation.getCurrentRecipient();
        
        // Calculate expected total (contribution amount × number of members)
        BigDecimal expectedTotal = rotation.getAmount()
            .multiply(new BigDecimal(rotation.getMemberIds().length));
        
        // Create DTO for group cycle
        CreateGroupCycleDto cycleDto = new CreateGroupCycleDto();
        cycleDto.setGroupId(rotation.getGroupId());
        cycleDto.setRotationId(rotation.getId());
        cycleDto.setCyclePeriod(cyclePeriod);
        cycleDto.setRecipientUserId(recipientUserId);
        cycleDto.setContributionAmount(rotation.getAmount());
        cycleDto.setExpectedTotal(expectedTotal);
        cycleDto.setCollectionStartDate(rotation.getCollectionDate());
        cycleDto.setCollectionEndDate(rotation.getCollectionDate());
        cycleDto.setPayoutDate(rotation.getPayoutDate());
        
        return groupCycleService.createGroupCycle(cycleDto);
    }

    public void updateRotation(UUID id) {
        Rotation rotation = rotationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rotation not found with ID: " + id));

        if (rotation.getPosition() >= rotation.getMemberIds().length - 1) {
            rotation.setStatus("complete");
            logger.info("Rotation completed with ID: " + id);
            return;
        } else {
            rotation.advancePosition();
        }
        //Create a new Cycle

        rotation.advanceDates();

        String cyclePeriod = rotation.getCollectionDate().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
        );
        
        // Get current recipient based on rotation position
        UUID recipientUserId = rotation.getCurrentRecipient();
        
        // Calculate expected total (contribution amount × number of members)
        BigDecimal expectedTotal = rotation.getAmount()
            .multiply(new BigDecimal(rotation.getMemberIds().length));
        
        // Create DTO for group cycle
        CreateGroupCycleDto cycleDto = new CreateGroupCycleDto();
        cycleDto.setGroupId(rotation.getGroupId());
        cycleDto.setRotationId(rotation.getId());
        cycleDto.setCyclePeriod(cyclePeriod);
        cycleDto.setRecipientUserId(recipientUserId);
        cycleDto.setContributionAmount(rotation.getAmount());
        cycleDto.setExpectedTotal(expectedTotal);
        cycleDto.setCollectionStartDate(rotation.getCollectionDate());
        cycleDto.setCollectionEndDate(rotation.getPayoutDate());

        GroupCycle cycle = groupCycleService.createGroupCycle(cycleDto);
        
        logger.info("New cycle with ID: " + cycle.getCycleId() + " created for rotation with ID: " + id);
        return;
    }

    public void validateCreateRotationRequest(CreateRotationDto request) {
        if (request.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than 0");
        }
        if (request.getCollectionDate() == null) {
            throw new IllegalArgumentException("Collection date cannot be null");
        }
        if (request.getMemberIds() == null || request.getMemberIds().length == 0) {
            throw new IllegalArgumentException("Member IDs cannot be empty");
        }
        if (request.getPayoutDate() == null) {
            throw new IllegalArgumentException("Payout date cannot be null");
        }
        if (request.getPayoutDate().isBefore(request.getCollectionDate())) {
            throw new IllegalArgumentException("Payout date must be after collection date");
        }
        if (request.getPayoutDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Payout date must be in the future");
        }
    }



}
