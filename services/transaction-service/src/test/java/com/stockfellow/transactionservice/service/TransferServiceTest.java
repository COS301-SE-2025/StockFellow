package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateTransferDto;
import com.stockfellow.transactionservice.dto.ProcessTransferDto;
import com.stockfellow.transactionservice.integration.PaystackService;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferResponse;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferResponse;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferResponse.PaystackTransferData;

import com.stockfellow.transactionservice.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private PayoutDetailsRepository payoutDetailsRepository;

    @Mock
    private GroupCycleRepository groupCycleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaystackService paystackService;

    @InjectMocks
    private TransferService transferService;

    private UUID testUserId;
    private UUID testCycleId;
    private UUID testPayoutDetailId;
    private UUID testTransferId;
    private CreateTransferDto createTransferDto;
    private Transfer transfer;
    private User user;
    private GroupCycle groupCycle;
    private PayoutDetails payoutDetails;
    private PaystackTransferData paystackResponseData;
    private PaystackTransferResponse paystackTransferResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCycleId = UUID.randomUUID();
        testPayoutDetailId = UUID.randomUUID();
        testTransferId = UUID.randomUUID();

        // Set up configuration values
        ReflectionTestUtils.setField(transferService, "maxRetryCount", 3);
        ReflectionTestUtils.setField(transferService, "defaultCurrency", "ZAR");

        // Create test DTOs
        createTransferDto = new CreateTransferDto();
        createTransferDto.setCycleId(testCycleId);
        createTransferDto.setUserId(testUserId);
        createTransferDto.setPayoutDetailId(testPayoutDetailId);
        createTransferDto.setAmount(new BigDecimal("1000.00"));
        createTransferDto.setReason("Test transfer");

        // Create test entities
        user = new User();
        user.setUserId(testUserId);
        user.setEmail("test@example.com");
        user.setStatus(User.UserStatus.active);

        groupCycle = new GroupCycle();
        groupCycle.setCycleId(testCycleId);
        groupCycle.setStatus("collection_complete");

        payoutDetails = new PayoutDetails();
        payoutDetails.setPayoutId(testPayoutDetailId);
        payoutDetails.setUserId(testUserId);
        payoutDetails.setIsVerified(true);
        payoutDetails.setRecipientCode("RCP_test123");

        transfer = new Transfer();
        transfer.setTransferId(testTransferId);
        transfer.setCycleId(testCycleId);
        transfer.setUserId(testUserId);
        transfer.setPayoutDetailId(testPayoutDetailId);
        transfer.setAmount(new BigDecimal("1000.00"));
        transfer.setCurrency("ZAR");
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        transfer.setRetryCount(0);

        // Create Paystack response
        paystackResponseData = new PaystackTransferData();
        paystackResponseData.setReference("TXF_test123");
        paystackResponseData.setTransferCode("TRF_test456");
        paystackResponseData.setStatus("success");

        paystackTransferResponse = new PaystackTransferResponse();
        paystackTransferResponse.setStatus(true);
        paystackTransferResponse.setData(paystackResponseData);
        paystackTransferResponse.setMessage("Transfer successful");
    }

    @Test
    void createTransfer_WhenAllValidationsPass_ShouldCreateSuccessfulTransfer() throws Exception {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));
        when(transferRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transfer.TransferStatus.COMPLETED))
            .thenReturn(Collections.emptyList());
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(paystackService.initiateTransfer(any(PaystackTransferRequest.class))).thenReturn(paystackTransferResponse);

        // When
        Transfer result = transferService.createTransfer(createTransferDto);

        // Then
        assertNotNull(result);
        assertEquals(testCycleId, result.getCycleId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testPayoutDetailId, result.getPayoutDetailId());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals("ZAR", result.getCurrency());

        verify(transferRepository, times(1)).save(any(Transfer.class)); // Once for initial save, once for update after Paystack
        verify(paystackService).initiateTransfer(any(PaystackTransferRequest.class));

        // Verify Paystack request
        ArgumentCaptor<PaystackTransferRequest> requestCaptor = ArgumentCaptor.forClass(PaystackTransferRequest.class);
        verify(paystackService).initiateTransfer(requestCaptor.capture());
        PaystackTransferRequest capturedRequest = requestCaptor.getValue();
        assertEquals(100000, capturedRequest.getAmount()); // 1000 * 100 = 100000 kobo
        assertEquals("RCP_test123", capturedRequest.getRecipient());
        assertEquals("Test transfer", capturedRequest.getReason());
    }

    @Test
    void createTransfer_WhenCycleNotFound_ShouldThrowException() {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("Cycle not found with ID: " + testCycleId, exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenCycleNotComplete_ShouldThrowException() {
        // Given
        groupCycle.setStatus("active");
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("Cycle is not ready for transfer. Status: active", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenPayoutDetailsNotFound_ShouldThrowException() {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("Payout details not found with ID: " + testPayoutDetailId, exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenPayoutDetailsNotVerified_ShouldThrowException() {
        // Given
        payoutDetails.setIsVerified(false);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("Payout details are not verified", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenPayoutDetailsWrongUser_ShouldThrowException() {
        // Given
        payoutDetails.setUserId(UUID.randomUUID());
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("Payout details do not belong to the specified user", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenNoRecipientCode_ShouldThrowException() {
        // Given
        payoutDetails.setRecipientCode(null);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("No transfer recipient code found for payer. Please register as transfer recipient first.", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createTransfer_WhenTransferAlreadyExists_ShouldThrowException() {
        // Given
        Transfer existingTransfer = new Transfer();
        existingTransfer.setStatus(Transfer.TransferStatus.COMPLETED);
        
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));
        when(transferRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transfer.TransferStatus.COMPLETED))
            .thenReturn(List.of(existingTransfer));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.createTransfer(createTransferDto));

        assertEquals("User has already completed received a transfer (payout) for this cycle", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    // @Test
    // void createTransfer_WhenPaystackFails_ShouldSetFailedStatus() throws Exception {
    //     // Given
    //     paystackTransferResponse.setStatus(false);
    //     paystackTransferResponse.setMessage("Insufficient funds");
        
    //     when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
    //     when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
    //     when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));
    //     when(transferRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transfer.TransferStatus.COMPLETED))
    //         .thenReturn(Collections.emptyList());
    //     when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
    //     when(paystackService.initiateTransfer(any(PaystackTransferRequest.class))).thenReturn(paystackTransferResponse);

    //     // When
    //     Transfer result = transferService.createTransfer(createTransferDto);

    //     // Then
    //     verify(transferRepository, times(1)).save(any(Transfer.class));
        
    //     // Verify the second save call sets failed status
    //     ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
    //     verify(transferRepository, times(1
    //     )).save(transferCaptor.capture());
    //     Transfer savedTransfer = transferCaptor.getAllValues().get(1);
    //     assertEquals(Transfer.TransferStatus.FAILED, savedTransfer.getStatus());
    //     assertEquals("Insufficient funds", savedTransfer.getFailureReason());
    //     assertEquals("failed", savedTransfer.getGatewayStatus());
    // }

    // @Test
    // void createTransfer_WhenPaystackThrowsException_ShouldSetFailedStatus() throws Exception {
    //     // Given
    //     when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
    //     when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
    //     when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));
    //     when(transferRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transfer.TransferStatus.COMPLETED))
    //         .thenReturn(Collections.emptyList());
    //     when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
    //     when(paystackService.initiateTransfer(any(PaystackTransferRequest.class)))
    //         .thenThrow(new RuntimeException("Network error"));

    //     // When
    //     Transfer result = transferService.createTransfer(createTransferDto);

    //     // Then
    //     verify(transferRepository, times(1)).save(any(Transfer.class));
        
    //     ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
    //     verify(transferRepository, times(1)).save(transferCaptor.capture());
    //     Transfer savedTransfer = transferCaptor.getAllValues().get(1);
    //     assertEquals(Transfer.TransferStatus.FAILED, savedTransfer.getStatus());
    //     assertTrue(savedTransfer.getFailureReason().contains("Exception during transfer"));
    //     assertEquals("error", savedTransfer.getGatewayStatus());
    // }

    @Test
    void createTransfer_WhenCurrencyNotProvided_ShouldUseDefaultCurrency() throws Exception {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.findById(testPayoutDetailId)).thenReturn(Optional.of(payoutDetails));
        when(transferRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transfer.TransferStatus.COMPLETED))
            .thenReturn(Collections.emptyList());
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(paystackService.initiateTransfer(any(PaystackTransferRequest.class))).thenReturn(paystackTransferResponse);

        // When
        Transfer result = transferService.createTransfer(createTransferDto);

        // Then
        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository, atLeastOnce()).save(transferCaptor.capture());
        Transfer savedTransfer = transferCaptor.getAllValues().get(0);
        assertEquals("ZAR", savedTransfer.getCurrency());
    }

    @Test
    void processTransfer_WhenSuccessful_ShouldUpdateTransferStatus() {
        // Given
        ProcessTransferDto processDto = new ProcessTransferDto();
        processDto.setStatus(Transfer.TransferStatus.COMPLETED);
        processDto.setPaystackTransferCode("TRF_123");
        processDto.setPaystackRecipientCode("RCP_123");

        when(transferRepository.findById(testTransferId)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(groupCycleRepository.save(any(GroupCycle.class))).thenReturn(groupCycle);

        // When
        Transfer result = transferService.processTransfer(testTransferId, processDto);

        // Then
        assertNotNull(result);
        verify(transferRepository).save(any(Transfer.class));
        
        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        Transfer savedTransfer = transferCaptor.getValue();
        assertEquals(Transfer.TransferStatus.COMPLETED, savedTransfer.getStatus());
        assertEquals("TRF_123", savedTransfer.getPaystackTransferCode());
        assertEquals("RCP_123", savedTransfer.getPaystackRecipientCode());
        assertNotNull(savedTransfer.getCompletedAt());

        // Verify cycle status update
        verify(groupCycleRepository).save(any(GroupCycle.class));
    }

    @Test
    void processTransfer_WhenTransferNotFound_ShouldThrowException() {
        // Given
        ProcessTransferDto processDto = new ProcessTransferDto();
        when(transferRepository.findById(testTransferId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.processTransfer(testTransferId, processDto));

        assertEquals("Transfer not found with ID: " + testTransferId, exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void findById_WhenTransferExists_ShouldReturnTransfer() {
        // Given
        when(transferRepository.findById(testTransferId)).thenReturn(Optional.of(transfer));

        // When
        Transfer result = transferService.findById(testTransferId);

        // Then
        assertNotNull(result);
        assertEquals(transfer, result);
        verify(transferRepository).findById(testTransferId);
    }

    @Test
    void findById_WhenTransferNotFound_ShouldThrowException() {
        // Given
        when(transferRepository.findById(testTransferId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.findById(testTransferId));

        assertEquals("Transfer not found with ID: " + testTransferId, exception.getMessage());
    }

    @Test
    void findByCycleId_ShouldReturnTransferList() {
        // Given
        List<Transfer> transfers = Arrays.asList(transfer);
        when(transferRepository.findByCycleId(testCycleId)).thenReturn(transfers);

        // When
        List<Transfer> result = transferService.findByCycleId(testCycleId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transfer, result.get(0));
        verify(transferRepository).findByCycleId(testCycleId);
    }

    @Test
    void findByUserId_ShouldReturnPagedTransfers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfer> page = new PageImpl<>(Arrays.asList(transfer));
        when(transferRepository.findByUserId(testUserId, pageable)).thenReturn(page);

        // When
        Page<Transfer> result = transferService.findByUserId(testUserId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(transfer, result.getContent().get(0));
        verify(transferRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void findByStatus_ShouldReturnTransferList() {
        // Given
        List<Transfer> transfers = Arrays.asList(transfer);
        when(transferRepository.findByStatus(Transfer.TransferStatus.PENDING)).thenReturn(transfers);

        // When
        List<Transfer> result = transferService.findByStatus(Transfer.TransferStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transfer, result.get(0));
        verify(transferRepository).findByStatus(Transfer.TransferStatus.PENDING);
    }

    // @Test
    // void getTransferStatistics_ShouldReturnStatistics() {
    //     // Given
    //     List<Object[]> statsData = Arrays.asList(
    //         new Object[]{Transfer.TransferStatus.COMPLETED, 5L, new BigDecimal("5000.00")},
    //         new Object[]{Transfer.TransferStatus.FAILED, 2L, new BigDecimal("2000.00")},
    //         new Object[]{Transfer.TransferStatus.PENDING, 1L, new BigDecimal("1000.00")}
    //     );
    //     when(transferRepository.getTransferStatisticsByUser(testUserId)).thenReturn(statsData);

    //     // When
    //     TransferService.TransferStatistics result = transferService.getTransferStatistics(testUserId);

    //     // Then
    //     assertNotNull(result);
    //     assertEquals(8L, result.getTotalTransfers());
    //     assertEquals(5L, result.getCompletedTransfers());
    //     assertEquals(2L, result.getFailedTransfers());
    //     assertEquals(1L, result.getPendingTransfers());
    //     assertEquals(new BigDecimal("8000.00"), result.getTotalAmount());
    //     assertEquals(new BigDecimal("5000.00"), result.getCompletedAmount());
    // }

    @Test
    void cancelTransfer_WhenPending_ShouldCancelSuccessfully() {
        // Given
        String reason = "User requested cancellation";
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        when(transferRepository.findById(testTransferId)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        // When
        Transfer result = transferService.cancelTransfer(testTransferId, reason);

        // Then
        assertNotNull(result);
        verify(transferRepository).save(any(Transfer.class));
        
        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        Transfer savedTransfer = transferCaptor.getValue();
        assertEquals(Transfer.TransferStatus.CANCELLED, savedTransfer.getStatus());
        assertEquals("Cancelled: " + reason, savedTransfer.getFailureReason());
    }

    @Test
    void cancelTransfer_WhenCompleted_ShouldThrowException() {
        // Given
        transfer.setStatus(Transfer.TransferStatus.COMPLETED);
        when(transferRepository.findById(testTransferId)).thenReturn(Optional.of(transfer));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transferService.cancelTransfer(testTransferId, "reason"));

        assertEquals("Transfer cannot be cancelled in current status: COMPLETED", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    // @Test
    // void transferStatistics_FromRepositoryResult_ShouldCalculateCorrectly() {
    //     // Given
    //     List<Object[]> statsData = Arrays.asList(
    //         new Object[]{Transfer.TransferStatus.COMPLETED, 3L, new BigDecimal("3000.00")},
    //         new Object[]{Transfer.TransferStatus.CANCELLED, 1L, new BigDecimal("1000.00")},
    //         new Object[]{Transfer.TransferStatus.PROCESSING, 2L, new BigDecimal("2000.00")}
    //     );

    //     // When
    //     TransferService.TransferStatistics stats = TransferService.TransferStatistics.fromRepositoryResult(statsData);

    //     // Then
    //     assertEquals(6L, stats.getTotalTransfers());
    //     assertEquals(3L, stats.getCompletedTransfers());
    //     assertEquals(1L, stats.getFailedTransfers()); // CANCELLED counts as failed
    //     assertEquals(2L, stats.getPendingTransfers()); // PROCESSING counts as pending
    //     assertEquals(new BigDecimal("6000.00"), stats.getTotalAmount());
    //     assertEquals(new BigDecimal("3000.00"), stats.getCompletedAmount());
    // }

    @Test
    void transferStatistics_EmptyResult_ShouldReturnZeros() {
        // Given
        List<Object[]> emptyStats = Collections.emptyList();

        // When
        TransferService.TransferStatistics stats = TransferService.TransferStatistics.fromRepositoryResult(emptyStats);

        // Then
        assertEquals(0L, stats.getTotalTransfers());
        assertEquals(0L, stats.getCompletedTransfers());
        assertEquals(0L, stats.getFailedTransfers());
        assertEquals(0L, stats.getPendingTransfers());
        assertNull(stats.getTotalAmount()); // Will be null if not initialized
        assertNull(stats.getCompletedAmount());
    }
}