package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.*;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.*;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {
    
    @Autowired
    private TransferService transferService;
    
    @Autowired
    private ActivityLogService activityLogService;

    // Create transfer (triggered when cycle collection is complete)
    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransfer(@Valid @RequestBody CreateTransferDto createDto) {
        Transfer transfer = transferService.createTransfer(createDto);
        activityLogService.logActivity(transfer.getUserId(), transfer.getCycleId(), 
                                     ActivityLog.EntityType.TRANSFER, transfer.getTransferId(), 
                                     "TRANSFER_CREATED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(TransferResponseDto.fromEntity(transfer));
    }
    
    // Process transfer (handle gateway response)
    @PostMapping("/{transferId}/process")
    public ResponseEntity<TransferResponseDto> processTransfer(
            @PathVariable UUID transferId,
            @RequestBody ProcessTransferDto processDto) {
        Transfer transfer = transferService.processTransfer(transferId, processDto);
        return ResponseEntity.ok(TransferResponseDto.fromEntity(transfer));
    }
    
    // Get transfer details
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponseDto> getTransfer(@PathVariable UUID transferId) {
        Transfer transfer = transferService.findById(transferId);
        return ResponseEntity.ok(TransferResponseDto.fromEntity(transfer));
    }
    
    // Get transfers by cycle
    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<TransferResponseDto>> getTransfersByCycle(@PathVariable UUID cycleId) {
        List<Transfer> transfers = transferService.findByCycleId(cycleId);
        return ResponseEntity.ok(transfers.stream()
                               .map(TransferResponseDto::fromEntity)
                               .toList());
    }
    
    // Get transfers by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransferResponseDto>> getTransfersByUser(
            @PathVariable UUID userId, 
            Pageable pageable) {
        Page<Transfer> transfers = transferService.findByUserId(userId, pageable);
        return ResponseEntity.ok(transfers.map(TransferResponseDto::fromEntity));
    }
}
