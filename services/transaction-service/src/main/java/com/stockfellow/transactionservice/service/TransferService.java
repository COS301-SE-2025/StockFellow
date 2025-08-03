package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateTransferDto;
import com.stockfellow.transactionservice.dto.ProcessTransferDto;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.repository.*;
import com.stockfellow.transactionservice.integration.PaystackService;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@Transactional
public class TransferService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);
    
    @Autowired
    private TransferRepository transferRepository;
    
    @Autowired
    private PayoutDetailsRepository payoutDetailsRepository;
    
    @Autowired
    private GroupCycleRepository groupCycleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private PaystackService paystackService;
    
    @Value("${app.transfer.max-retry-count:3}")
    private Integer maxRetryCount;
    
    @Value("${app.transfer.default-currency:ZAR}")
    private String defaultCurrency;

    /**
     * Create a new transfer
     */
    public Transfer createTransfer(CreateTransferDto createDto) {
        logger.info("Creating transfer for cycle: {} and user: {}", createDto.getCycleId(), createDto.getUserId());
        
        // Validate cycle exists and is eligible for transfer
        GroupCycle cycle = validateCycleForTransfer(createDto.getCycleId());
        
        // Validate user exists
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + createDto.getUserId()));
        
        // Validate payout details exist and are verified
        PayoutDetails payoutDetails = validatePayoutDetails(createDto.getPayoutDetailId(), createDto.getUserId());
        
        // Create transfer entity
        Transfer transfer = new Transfer();
        transfer.setCycleId(createDto.getCycleId());
        transfer.setUserId(createDto.getUserId());
        transfer.setPayoutDetailId(createDto.getPayoutDetailId());
        transfer.setAmount(createDto.getAmount());
        transfer.setCurrency(createDto.getCurrency() != null ? createDto.getCurrency() : defaultCurrency);
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        transfer.setRetryCount(0);
        
        // Save transfer
        transfer = transferRepository.save(transfer);
        
        // Log activity
        // activityLogService.logActivity(
        //     createDto.getUserId(), 
        //     createDto.getCycleId(),
        //     ActivityLog.EntityType.TRANSFER, 
        //     transfer.getTransferId(),
        //     "TRANSFER_CREATED", 
        //     null, 
        //     null
        // );
        
        // Initiate transfer with payment gateway
        try {
            initiatePaystackTransfer(transfer, payoutDetails);
        } catch (Exception e) {
            logger.error("Failed to initiate transfer: {}", e.getMessage());
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason("Failed to initiate with payment gateway: " + e.getMessage());
            transfer = transferRepository.save(transfer);
        }
        
        logger.info("Transfer created successfully with ID: {}", transfer.getTransferId());
        return transfer;
    }

    /**
     * Process transfer response from payment gateway
     */
    public Transfer processTransfer(UUID transferId, ProcessTransferDto processDto) {
        logger.info("Processing transfer: {} with status: {}", transferId, processDto.getStatus());
        
        Transfer transfer = findById(transferId);
        Transfer.TransferStatus oldStatus = transfer.getStatus();
        
        // Update transfer with gateway response
        transfer.setPaystackTransferCode(processDto.getPaystackTransferCode());
        transfer.setPaystackRecipientCode(processDto.getPaystackRecipientCode());
        transfer.setStatus(processDto.getStatus());
        transfer.setGatewayStatus(processDto.getGatewayStatus());
        transfer.setFailureReason(processDto.getFailureReason());
        
        // Set completion time for successful transfers
        if (processDto.getStatus() == Transfer.TransferStatus.COMPLETED) {
            transfer.setCompletedAt(LocalDateTime.now());
        }
        
        transfer = transferRepository.save(transfer);
        
        // Handle status change actions
        handleTransferStatusChange(transfer, oldStatus);
        
        // Log activity
        // activityLogService.logActivity(
        //     transfer.getUserId(), 
        //     transfer.getCycleId(),
        //     ActivityLog.EntityType.TRANSFER, 
        //     transfer.getTransferId(),
        //     "TRANSFER_PROCESSED", 
        //     null, 
        //     null
        // );
        
        logger.info("Transfer processed successfully: {}", transferId);
        return transfer;
    }

    /**
     * Retry a failed transfer
     */
    public Transfer retryTransfer(UUID transferId) {
        logger.info("Retrying transfer: {}", transferId);
        
        Transfer transfer = findById(transferId);
        
        // Validate transfer can be retried
        if (transfer.getStatus() != Transfer.TransferStatus.FAILED) {
            throw new RuntimeException("Transfer is not in FAILED status");
        }
        
        if (transfer.getRetryCount() >= maxRetryCount) {
            throw new RuntimeException("Maximum retry count exceeded for transfer: " + transferId);
        }
        
        // Increment retry count
        transfer.setRetryCount(transfer.getRetryCount() + 1);
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        transfer.setFailureReason(null);
        transfer.setGatewayStatus(null);
        
        transfer = transferRepository.save(transfer);
        
        // Get payout details and retry
        PayoutDetails payoutDetails = payoutDetailsRepository.findById(transfer.getPayoutDetailId())
            .orElseThrow(() -> new RuntimeException("Payout details not found"));
        
        try {
            initiatePaystackTransfer(transfer, payoutDetails);
        } catch (Exception e) {
            logger.error("Failed to retry transfer: {}", e.getMessage());
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason("Retry failed: " + e.getMessage());
            transfer = transferRepository.save(transfer);
        }
        
        // Log activity
        // activityLogService.logActivity(
        //     transfer.getUserId(), 
        //     transfer.getCycleId(),
        //     ActivityLog.EntityType.TRANSFER, 
        //     transfer.getTransferId(),
        //     "TRANSFER_RETRIED", 
        //     null, 
        //     null
        // );
        
        logger.info("Transfer retry initiated: {}", transferId);
        return transfer;
    }

    /**
     * Find transfer by ID
     */
    @Transactional(readOnly = true)
    public Transfer findById(UUID transferId) {
        return transferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("Transfer not found with ID: " + transferId));
    }

    /**
     * Find transfers by cycle ID
     */
    @Transactional(readOnly = true)
    public List<Transfer> findByCycleId(UUID cycleId) {
        return transferRepository.findByCycleId(cycleId);
    }

    /**
     * Find transfers by user ID (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Transfer> findByUserId(UUID userId, Pageable pageable) {
        return transferRepository.findByUserId(userId, pageable);
    }

    /**
     * Find transfers by status
     */
    @Transactional(readOnly = true)
    public List<Transfer> findByStatus(Transfer.TransferStatus status) {
        return transferRepository.findByStatus(status);
    }

    /**
     * Get transfer statistics for a user
     */
    @Transactional(readOnly = true)
    public TransferStatistics getTransferStatistics(UUID userId) {
        List<Object[]> stats = transferRepository.getTransferStatisticsByUser(userId);
        return TransferStatistics.fromRepositoryResult(stats);
    }

    /**
     * Process automatic transfers for completed cycles
     */
    @Transactional
    public void processAutomaticTransfers() {
        logger.info("Processing automatic transfers for completed cycles");
        
        // Find cycles that are collection complete but haven't been transferred
        List<GroupCycle> completedCycles = groupCycleRepository.findByStatus(GroupCycle.CycleStatus.COLLECTION_COMPLETE);
        
        for (GroupCycle cycle : completedCycles) {
            try {
                processAutomaticTransferForCycle(cycle);
            } catch (Exception e) {
                logger.error("Failed to process automatic transfer for cycle: {}", cycle.getCycleId(), e);
            }
        }
    }

    /**
     * Find and retry failed transfers
     */
    @Transactional
    public void retryFailedTransfers() {
        logger.info("Retrying failed transfers");
        
        List<Transfer> retryableTransfers = transferRepository.findRetryableTransfers(maxRetryCount);
        
        for (Transfer transfer : retryableTransfers) {
            try {
                retryTransfer(transfer.getTransferId());
            } catch (Exception e) {
                logger.error("Failed to retry transfer: {}", transfer.getTransferId(), e);
            }
        }
    }

    /**
     * Cancel a pending transfer
     */
    public Transfer cancelTransfer(UUID transferId, String reason) {
        logger.info("Cancelling transfer: {} with reason: {}", transferId, reason);
        
        Transfer transfer = findById(transferId);
        
        if (transfer.getStatus() != Transfer.TransferStatus.PENDING && 
            transfer.getStatus() != Transfer.TransferStatus.PROCESSING) {
            throw new RuntimeException("Transfer cannot be cancelled in current status: " + transfer.getStatus());
        }
        
        transfer.setStatus(Transfer.TransferStatus.CANCELLED);
        transfer.setFailureReason("Cancelled: " + reason);
        
        transfer = transferRepository.save(transfer);
        
        // Log activity
        // activityLogService.logActivity(
        //     transfer.getUserId(), 
        //     transfer.getCycleId(),
        //     ActivityLog.EntityType.TRANSFER, 
        //     transfer.getTransferId(),
        //     "TRANSFER_CANCELLED", 
        //     null, 
        //     null
        // );
        
        logger.info("Transfer cancelled: {}", transferId);
        return transfer;
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Validate cycle for transfer eligibility
     */
    private GroupCycle validateCycleForTransfer(UUID cycleId) {
        GroupCycle cycle = groupCycleRepository.findById(cycleId)
            .orElseThrow(() -> new RuntimeException("Cycle not found with ID: " + cycleId));
        
        if (cycle.getStatus() != GroupCycle.CycleStatus.COLLECTION_COMPLETE) {
            throw new RuntimeException("Cycle is not ready for transfer. Status: " + cycle.getStatus());
        }
        
        return cycle;
    }

    /**
     * Validate payout details
     */
    private PayoutDetails validatePayoutDetails(UUID payoutDetailId, UUID userId) {
        PayoutDetails payoutDetails = payoutDetailsRepository.findById(payoutDetailId)
            .orElseThrow(() -> new RuntimeException("Payout details not found with ID: " + payoutDetailId));
        
        if (!payoutDetails.getUserId().equals(userId)) {
            throw new RuntimeException("Payout details do not belong to the specified user");
        }
        
        if (!payoutDetails.getIsVerified()) {
            throw new RuntimeException("Payout details are not verified");
        }
        
        return payoutDetails;
    }

    /**
     * Initiate transfer with Paystack
     */
    private void initiatePaystackTransfer(Transfer transfer, PayoutDetails payoutDetails) {
        logger.info("Initiating Paystack transfer for: {}", transfer.getTransferId());
        
        // Create Paystack transfer request
        PaystackTransferRequest request = new PaystackTransferRequest();
        request.setAmount(transfer.getAmount().multiply(new BigDecimal("100")).intValue()); // Convert to kobo
        request.setCurrency(transfer.getCurrency());
        request.setRecipient(payoutDetails.getRecipientCode());
        request.setReason("Group cycle payout - Cycle: " + transfer.getCycleId());
        request.setReference(generateTransferReference(transfer));
        
        try {
            PaystackTransferResponse response = paystackService.initiateTransfer(request);
            
            // Update transfer with Paystack response
            transfer.setPaystackTransferCode(response.getTransferCode());
            transfer.setStatus(Transfer.TransferStatus.PROCESSING);
            transfer.setInitiatedAt(LocalDateTime.now());
            
            transferRepository.save(transfer);
            
            logger.info("Paystack transfer initiated successfully: {}", transfer.getTransferId());
            
        } catch (Exception e) {
            logger.error("Failed to initiate Paystack transfer: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate transfer with Paystack: " + e.getMessage());
        }
    }

    /**
     * Handle transfer status changes
     */
    private void handleTransferStatusChange(Transfer transfer, Transfer.TransferStatus oldStatus) {
        if (transfer.getStatus() == Transfer.TransferStatus.COMPLETED && oldStatus != Transfer.TransferStatus.COMPLETED) {
            // Update cycle status to completed
            updateCycleStatus(transfer.getCycleId());
            
            // Send notification
            // notificationService.sendTransferCompletedNotification(transfer);
            
        } else if (transfer.getStatus() == Transfer.TransferStatus.FAILED) {
            // Send failure notification
            // notificationService.sendTransferFailedNotification(transfer);
        }
    }

    /**
     * Update cycle status after successful transfer
     */
    private void updateCycleStatus(UUID cycleId) {
        Optional<GroupCycle> cycleOpt = groupCycleRepository.findById(cycleId);
        if (cycleOpt.isPresent()) {
            GroupCycle cycle = cycleOpt.get();
            cycle.setStatus(GroupCycle.CycleStatus.COMPLETED);
            groupCycleRepository.save(cycle);
        }
    }

    /**
     * Process automatic transfer for a completed cycle
     */
    private void processAutomaticTransferForCycle(GroupCycle cycle) {
        logger.info("Processing automatic transfer for cycle: {}", cycle.getCycleId());
        
        // Check if transfer already exists
        List<Transfer> existingTransfers = transferRepository.findByCycleId(cycle.getCycleId());
        if (!existingTransfers.isEmpty()) {
            logger.info("Transfer already exists for cycle: {}", cycle.getCycleId());
            return;
        }
        
        // Get default payout method for recipient
        Optional<PayoutDetails> defaultPayoutOpt = payoutDetailsRepository
            .findByUserIdAndIsDefaultTrue(cycle.getRecipientUserId());
        
        if (!defaultPayoutOpt.isPresent()) {
            logger.warn("No default payout method found for user: {}", cycle.getRecipientUserId());
            return;
        }
        
        PayoutDetails payoutDetails = defaultPayoutOpt.get();
        
        // Create transfer DTO
        CreateTransferDto createDto = new CreateTransferDto();
        createDto.setCycleId(cycle.getCycleId());
        createDto.setUserId(cycle.getRecipientUserId());
        createDto.setPayoutDetailId(payoutDetails.getPayoutId());
        createDto.setAmount(cycle.getCurrentTotal());
        createDto.setCurrency(defaultCurrency);
        
        // Create transfer
        createTransfer(createDto);
    }

    /**
     * Generate unique transfer reference
     */
    private String generateTransferReference(Transfer transfer) {
        return "TXF-" + transfer.getTransferId().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    // ===== INNER CLASSES =====

    /**
     * Transfer statistics wrapper
     */
    public static class TransferStatistics {
        private long totalTransfers;
        private long completedTransfers;
        private long failedTransfers;
        private long pendingTransfers;
        private BigDecimal totalAmount;
        private BigDecimal completedAmount;
        
        public static TransferStatistics fromRepositoryResult(List<Object[]> stats) {
            TransferStatistics transferStats = new TransferStatistics();
            
            for (Object[] stat : stats) {
                Transfer.TransferStatus status = (Transfer.TransferStatus) stat[0];
                Long count = (Long) stat[1];
                BigDecimal amount = (BigDecimal) stat[2];
                
                transferStats.totalTransfers += count;
                transferStats.totalAmount = transferStats.totalAmount.add(amount);
                
                switch (status) {
                    case COMPLETED:
                        transferStats.completedTransfers = count;
                        transferStats.completedAmount = amount;
                        break;
                    case FAILED:
                    case CANCELLED:
                        transferStats.failedTransfers += count;
                        break;
                    case PENDING:
                    case PROCESSING:
                        transferStats.pendingTransfers += count;
                        break;
                }
            }
            
            return transferStats;
        }

        // Getters
        public long getTotalTransfers() { return totalTransfers; }
        public long getCompletedTransfers() { return completedTransfers; }
        public long getFailedTransfers() { return failedTransfers; }
        public long getPendingTransfers() { return pendingTransfers; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getCompletedAmount() { return completedAmount; }
    }
}