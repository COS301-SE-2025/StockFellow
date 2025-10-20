package com.stockfellow.transactionservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.stockfellow.transactionservice.dto.CreateGroupCycleDto;
import com.stockfellow.transactionservice.dto.GroupCycleResponseDto;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class GroupCycleServiceTest {

    @Mock
    private GroupCycleRepository groupCycleRepository;

    @InjectMocks
    private GroupCycleService groupCycleService;

    private CreateGroupCycleDto validDto;
    private GroupCycle groupCycle;
    private UUID groupId;
    private UUID recipientId;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();
        recipientId = UUID.randomUUID();

        // Create valid DTO
        validDto = new CreateGroupCycleDto();
        validDto.setGroupId(groupId);
        validDto.setCyclePeriod("2025-09");
        validDto.setRecipientUserId(recipientId);
        validDto.setContributionAmount(new BigDecimal("100.00"));
        validDto.setExpectedTotal(new BigDecimal("1000.00"));
        validDto.setCollectionStartDate(LocalDate.now().plusDays(1));
        validDto.setCollectionEndDate(LocalDate.now().plusDays(30));

        // Create sample GroupCycle
        groupCycle = new GroupCycle(
            groupId,
            "2025-09",
            recipientId,
            new BigDecimal("100.00"),
            new BigDecimal("1000.00"),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(30)
        );
    }

    @Test
    void createGroupCycle_ValidRequest_Success() {
        when(groupCycleRepository.findByGroupIdAndCyclePeriod(any(), any())).thenReturn(Optional.empty());
        when(groupCycleRepository.save(any())).thenReturn(groupCycle);

        GroupCycle result = groupCycleService.createGroupCycle(validDto);

        assertNotNull(result);
        assertEquals(groupId, result.getGroupId());
        verify(groupCycleRepository).save(any(GroupCycle.class));
    }

    @Test
    void createGroupCycle_DuplicateCycle_ThrowsException() {
        when(groupCycleRepository.findByGroupIdAndCyclePeriod(any(), any())).thenReturn(Optional.of(groupCycle));

        assertThrows(IllegalStateException.class, () -> groupCycleService.createGroupCycle(validDto));
    }

    @Test
    void getCycleById_ExistingCycle_ReturnsGroupCycle() {
        UUID cycleId = UUID.randomUUID();
        when(groupCycleRepository.findById(cycleId)).thenReturn(Optional.of(groupCycle));

        GroupCycle result = groupCycleService.getCycleById(cycleId);

        assertNotNull(result);
        assertEquals(groupId, result.getGroupId());
    }

    @Test
    void getCyclesByGroup_ReturnsListOfCycles() {
        when(groupCycleRepository.findByGroupIdOrderByCollectionStartDateDesc(groupId))
            .thenReturn(Arrays.asList(groupCycle));

        List<GroupCycle> result = groupCycleService.getCyclesByGroup(groupId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getCyclesByStatus_ReturnsListOfCycles() {
        when(groupCycleRepository.findByStatus("ACTIVE"))
            .thenReturn(Arrays.asList(groupCycle));

        List<GroupCycleResponseDto> result = groupCycleService.getCyclesByStatus("ACTIVE");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getNextCycleForGroup_ValidRequest_ReturnsCycle() {
        when(groupCycleRepository.findFirstByGroupIdAndStatusOrderByCollectionStartDateAsc(groupId, "PENDING"))
            .thenReturn(Optional.of(groupCycle));

        GroupCycle result = groupCycleService.getNextCycleForGroup(groupId, "PENDING");

        assertNotNull(result);
        assertEquals(groupId, result.getGroupId());
    }

    @Test
    void updateCycleStatus_ValidUpdate_Success() {
        UUID cycleId = UUID.randomUUID();
        when(groupCycleRepository.findById(cycleId)).thenReturn(Optional.of(groupCycle));
        when(groupCycleRepository.save(any(GroupCycle.class))).thenReturn(groupCycle);

        GroupCycle result = groupCycleService.updateCycleStatus(cycleId, "COMPLETED");

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(groupCycleRepository).save(any(GroupCycle.class));
    }

    @Test
    void validateCreateCycleRequest_InvalidData_ThrowsException() {
        CreateGroupCycleDto invalidDto = new CreateGroupCycleDto();
        assertThrows(IllegalArgumentException.class, () -> 
            groupCycleService.createGroupCycle(invalidDto));
    }
}