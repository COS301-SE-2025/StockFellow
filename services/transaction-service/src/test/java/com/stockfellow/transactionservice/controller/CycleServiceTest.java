package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateCycleRequest;
import com.stockfellow.transactionservice.dto.CycleResponse;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CycleService Tests")
class CycleServiceTest {

    @Mock
    private GroupCycleRepository cycleRepository;

    @InjectMocks
    private CycleService cycleService;

    private UUID testGroupId;
    private UUID testRecipientId;
    private UUID testPaymentMethodId;
    private UUID testCycleId;
    private String testCycleMonth;
    private CreateCycleRequest validRequest;
    private GroupCycle testCycle;

    @BeforeEach
    void setUp() {
        testGroupId = UUID.randomUUID();
        testRecipientId = UUID.randomUUID();
        testPaymentMethodId = UUID.randomUUID();
        testCycleId = UUID.randomUUID();
        testCycleMonth = "2025-07";

        validRequest = new CreateCycleRequest();
        validRequest.setGroupId(testGroupId);
        validRequest.setCycleMonth(testCycleMonth);
        validRequest.setRecipientUserId(testRecipientId);
        validRequest.setRecipientPaymentMethodId(testPaymentMethodId);
        validRequest.setContributionAmount(new BigDecimal("100.00"));
        validRequest.setCollectionDate(LocalDate.now().plusDays(10));
        validRequest.setTotalExpectedAmount(new BigDecimal("1000.00"));

        testCycle = new GroupCycle();
        testCycle.setCycleId(testCycleId);
        testCycle.setGroupId(testGroupId);
        testCycle.setCycleMonth(testCycleMonth);
        testCycle.setRecipientUserId(testRecipientId);
        testCycle.setRecipientPaymentMethodId(testPaymentMethodId);
        testCycle.setContributionAmount(new BigDecimal("100.00"));
        testCycle.setCollectionDate(LocalDate.now().plusDays(10));
        testCycle.setTotalExpectedAmount(new BigDecimal("1000.00"));
        testCycle.setStatus("PENDING");
        testCycle.setSuccessfulPayments(0);
        testCycle.setFailedPayments(0);
    }

    @Test
    @DisplayName("Should create group cycle successfully when valid request is provided")
    void createGroupCycle_WithValidRequest_ShouldReturnCreatedCycle() {
        // Given
        when(cycleRepository.findByGroupIdAndCycleMonth(testGroupId, testCycleMonth))
            .thenReturn(Optional.empty());
        when(cycleRepository.save(any(GroupCycle.class))).thenReturn(testCycle);

        // When
        GroupCycle result = cycleService.createGroupCycle(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(testGroupId, result.getGroupId());
        assertEquals(testCycleMonth, result.getCycleMonth());
        assertEquals(testRecipientId, result.getRecipientUserId());
        assertEquals(testPaymentMethodId, result.getRecipientPaymentMethodId());
        assertEquals(new BigDecimal("100.00"), result.getContributionAmount());
        assertEquals(new BigDecimal("1000.00"), result.getTotalExpectedAmount());
        assertEquals("PENDING", result.getStatus());
        assertEquals(0, result.getSuccessfulPayments());
        assertEquals(0, result.getFailedPayments());

        verify(cycleRepository).findByGroupIdAndCycleMonth(testGroupId, testCycleMonth);
        verify(cycleRepository).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when cycle already exists for group and month")
    void createGroupCycle_WhenCycleAlreadyExists_ShouldThrowException() {
        // Given
        when(cycleRepository.findByGroupIdAndCycleMonth(testGroupId, testCycleMonth))
            .thenReturn(Optional.of(testCycle));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Cycle already exists for group " + testGroupId + " in month " + testCycleMonth,
            exception.getMessage());

        verify(cycleRepository).findByGroupIdAndCycleMonth(testGroupId, testCycleMonth);
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when group ID is null")
    void createGroupCycle_WithNullGroupId_ShouldThrowException() {
        // Given
        validRequest.setGroupId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Group ID cannot be null", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when cycle month is null")
    void createGroupCycle_WithNullCycleMonth_ShouldThrowException() {
        // Given
        validRequest.setCycleMonth(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Cycle Month cannot be null or empty", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when cycle month is empty")
    void createGroupCycle_WithEmptyCycleMonth_ShouldThrowException() {
        // Given
        validRequest.setCycleMonth("   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Cycle Month cannot be null or empty", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when recipient user ID is null")
    void createGroupCycle_WithNullRecipientUserId_ShouldThrowException() {
        // Given
        validRequest.setRecipientUserId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Recipient ID cannot be null", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when recipient payment method ID is null")
    void createGroupCycle_WithNullRecipientPaymentMethodId_ShouldThrowException() {
        // Given
        validRequest.setRecipientPaymentMethodId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Recipient Payment method ID cannot be null", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when contribution amount is null")
    void createGroupCycle_WithNullContributionAmount_ShouldThrowException() {
        // Given
        validRequest.setContributionAmount(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Contribution amount must be greater than 0", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when contribution amount is zero or negative")
    void createGroupCycle_WithZeroContributionAmount_ShouldThrowException() {
        // Given
        validRequest.setContributionAmount(BigDecimal.ZERO);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Contribution amount must be greater than 0", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when collection date is null")
    void createGroupCycle_WithNullCollectionDate_ShouldThrowException() {
        // Given
        validRequest.setCollectionDate(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Collection Date cannot be null", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when collection date is in the past")
    void createGroupCycle_WithPastCollectionDate_ShouldThrowException() {
        // Given
        validRequest.setCollectionDate(LocalDate.now().minusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Collection date must be in the future", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when total expected amount is null")
    void createGroupCycle_WithNullTotalExpectedAmount_ShouldThrowException() {
        // Given
        validRequest.setTotalExpectedAmount(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.createGroupCycle(validRequest));

        assertEquals("Total expected amount must be greater than 0", exception.getMessage());
        verify(cycleRepository, never()).save(any(GroupCycle.class));
    }

    @Test
    @DisplayName("Should return cycle by ID when found")
    void getCycleById_WithValidId_ShouldReturnCycle() {
        // Given
        when(cycleRepository.findById(testCycleId)).thenReturn(Optional.of(testCycle));

        // When
        GroupCycle result = cycleService.getCycleById(testCycleId);

        // Then
        assertNotNull(result);
        assertEquals(testCycleId, result.getCycleId());
        verify(cycleRepository).findById(testCycleId);
    }

    @Test
    @DisplayName("Should throw exception when cycle not found by ID")
    void getCycleById_WithInvalidId_ShouldThrowException() {
        // Given
        when(cycleRepository.findById(testCycleId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.getCycleById(testCycleId));

        assertEquals("Cycle not found with ID: " + testCycleId, exception.getMessage());
        verify(cycleRepository).findById(testCycleId);
    }

    @Test
    @DisplayName("Should return all cycles successfully")
    void getAllCycles_ShouldReturnAllCycles() {
        // Given
        List<GroupCycle> cycles = Arrays.asList(testCycle);
        when(cycleRepository.findAll()).thenReturn(cycles);

        // When
        List<CycleResponse> result = cycleService.getAllCycles();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cycleRepository).findAll();
    }

    @Test
    @DisplayName("Should return cycles by group ID")
    void getCyclesByGroup_WithValidGroupId_ShouldReturnCycles() {
        // Given
        List<GroupCycle> cycles = Arrays.asList(testCycle);
        when(cycleRepository.findByGroupIdOrderByCollectionDateDesc(testGroupId)).thenReturn(cycles);

        // When
        List<CycleResponse> result = cycleService.getCyclesByGroup(testGroupId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cycleRepository).findByGroupIdOrderByCollectionDateDesc(testGroupId);
    }

    @Test
    @DisplayName("Should return cycles by status")
    void getCyclesByStatus_WithValidStatus_ShouldReturnCycles() {
        // Given
        String status = "PENDING";
        List<GroupCycle> cycles = Arrays.asList(testCycle);
        when(cycleRepository.findByStatus(status)).thenReturn(cycles);

        // When
        List<CycleResponse> result = cycleService.getCyclesByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cycleRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should return next cycle for group with status")
    void getNextCycleForGroup_WithValidGroupAndStatus_ShouldReturnCycle() {
        // Given
        String status = "PENDING";
        when(cycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, status))
            .thenReturn(Optional.of(testCycle));

        // When
        GroupCycle result = cycleService.getNextCycleForGroup(testGroupId, status);

        // Then
        assertNotNull(result);
        assertEquals(testCycleId, result.getCycleId());
        verify(cycleRepository).findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, status);
    }

    @Test
    @DisplayName("Should throw exception when no next cycle found for group")
    void getNextCycleForGroup_WhenNoCycleFound_ShouldThrowException() {
        // Given
        String status = "PENDING";
        when(cycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, status))
            .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.getNextCycleForGroup(testGroupId, status));

        assertEquals("No upcoming cycle found for group: " + testGroupId, exception.getMessage());
        verify(cycleRepository).findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, status);
    }

    @Test
    @DisplayName("Should return next upcoming cycle with status")
    void getNextUpcomingCycle_WithValidStatus_ShouldReturnCycle() {
        // Given
        String status = "PENDING";
        when(cycleRepository.findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
            eq(status), any(LocalDate.class))).thenReturn(Optional.of(testCycle));

        // When
        GroupCycle result = cycleService.getNextUpcomingCycle(status);

        // Then
        assertNotNull(result);
        assertEquals(testCycleId, result.getCycleId());
        verify(cycleRepository).findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
            eq(status), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should throw exception when no upcoming cycle found")
    void getNextUpcomingCycle_WhenNoCycleFound_ShouldThrowException() {
        // Given
        String status = "PENDING";
        when(cycleRepository.findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
            eq(status), any(LocalDate.class))).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.getNextUpcomingCycle(status));

        assertEquals("No upcoming cycle found with status: " + status, exception.getMessage());
        verify(cycleRepository).findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
            eq(status), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should return cycle by group and month")
    void getCycleByGroupAndMonth_WithValidParams_ShouldReturnCycle() {
        // Given
        when(cycleRepository.findByGroupIdAndCycleMonth(testGroupId, testCycleMonth))
            .thenReturn(Optional.of(testCycle));

        // When
        GroupCycle result = cycleService.getCycleByGroupAndMonth(testGroupId, testCycleMonth);

        // Then
        assertNotNull(result);
        assertEquals(testCycleId, result.getCycleId());
        verify(cycleRepository).findByGroupIdAndCycleMonth(testGroupId, testCycleMonth);
    }

    @Test
    @DisplayName("Should throw exception when no cycle found for group and month")
    void getCycleByGroupAndMonth_WhenNoCycleFound_ShouldThrowException() {
        // Given
        when(cycleRepository.findByGroupIdAndCycleMonth(testGroupId, testCycleMonth))
            .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cycleService.getCycleByGroupAndMonth(testGroupId, testCycleMonth));

        assertEquals("No cycle found for group: " + testGroupId + " in month: " + testCycleMonth,
            exception.getMessage());
        verify(cycleRepository).findByGroupIdAndCycleMonth(testGroupId, testCycleMonth);
    }

    @Test
    @DisplayName("Should return earliest cycles for group")
    void getEarliestCyclesForGroup_WithValidGroupId_ShouldReturnCycles() {
        // Given
        List<GroupCycle> cycles = Arrays.asList(testCycle);
        when(cycleRepository.findFirstByGroupIdOrderByCollectionDateAsc(testGroupId)).thenReturn(cycles);

        // When
        List<CycleResponse> result = cycleService.getEarliestCyclesForGroup(testGroupId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cycleRepository).findFirstByGroupIdOrderByCollectionDateAsc(testGroupId);
    }

    @Test
    @DisplayName("Should update cycle status successfully")
    void updateCycleStatus_WithValidParams_ShouldUpdateStatus() {
        // Given
        String newStatus = "COMPLETED";
        when(cycleRepository.findById(testCycleId)).thenReturn(Optional.of(testCycle));
        when(cycleRepository.save(any(GroupCycle.class))).thenReturn(testCycle);

        // When
        GroupCycle result = cycleService.updateCycleStatus(testCycleId, newStatus);

        // Then
        assertNotNull(result);
        verify(cycleRepository).findById(testCycleId);
        verify(cycleRepository).save(argThat(cycle -> newStatus.equals(cycle.getStatus())));
    }

    @Test
    @DisplayName("Should update payment counts successfully")
    void updatePaymentCounts_WithValidParams_ShouldUpdateCounts() {
        // Given
        int successfulPayments = 5;
        int failedPayments = 2;
        when(cycleRepository.findById(testCycleId)).thenReturn(Optional.of(testCycle));
        when(cycleRepository.save(any(GroupCycle.class))).thenReturn(testCycle);

        // When
        GroupCycle result = cycleService.updatePaymentCounts(testCycleId, successfulPayments, failedPayments);

        // Then
        assertNotNull(result);
        verify(cycleRepository).findById(testCycleId);
        verify(cycleRepository).save(argThat(cycle -> 
            cycle.getSuccessfulPayments() == successfulPayments && 
            cycle.getFailedPayments() == failedPayments));
    }
}