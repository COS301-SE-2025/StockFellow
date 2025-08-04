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
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/payment-methods")
@CrossOrigin(origins = "*")
public class PaymentDetailsController {
    
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    
    // @Autowired
    // private ActivityLogService activityLogService;

    // === PAYER DETAILS (Card/Payment Methods) ===
    
    // Add new card (for changes/expiry)
    @PostMapping("/payer")
    public ResponseEntity<PayerDetailsResponseDto> addPayerDetails(
            @Valid @RequestBody CreatePayerDetailsDto createDto) {
        PayerDetails created = paymentDetailsService.addPayerDetails(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(PayerDetailsResponseDto.fromEntity(created));
    }
    
    @GetMapping("/payer/user/{userId}")
    public ResponseEntity<List<PayerDetailsResponseDto>> getPayerDetailsByUser(@PathVariable UUID userId) {
        List<PayerDetails> payerDetails = paymentDetailsService.findPayerDetailsByUserId(userId);
        return ResponseEntity.ok(payerDetails.stream()
                               .map(PayerDetailsResponseDto::fromEntity)
                               .toList());
    }
    
    @PutMapping("/payer/{payerId}")
    public ResponseEntity<PayerDetailsResponseDto> updatePayerDetails(
            @PathVariable UUID payerId,
            @Valid @RequestBody UpdatePayerDetailsDto updateDto) {
        PayerDetails payerDetails = paymentDetailsService.updatePayerDetails(payerId, updateDto);
        return ResponseEntity.ok(PayerDetailsResponseDto.fromEntity(payerDetails));
    }
    
    // Deactivate card (instead of delete)
    @PutMapping("/payer/{payerId}/deactivate")
    public ResponseEntity<PayerDetailsResponseDto> deactivateCard(@PathVariable UUID payerId) {
        PayerDetails deactivated = payerDetailsService.deactivateCard(payerId);
        return ResponseEntity.ok(PayerDetailsResponseDto.fromEntity(deactivated));
    }
    
    // === PAYOUT DETAILS (Bank/Recipient Methods) ===
    
    @PostMapping("/payout")
    public ResponseEntity<PayoutDetailsResponseDto> addPayoutDetails(@Valid @RequestBody CreatePayoutDetailsDto createDto) {
        PayoutDetails payoutDetails = paymentDetailsService.addPayoutDetails(createDto);
        activityLogService.logActivity(payoutDetails.getUserId(), null, 
                                     ActivityLog.EntityType.PAYOUT_DETAILS, payoutDetails.getPayoutId(), 
                                     "PAYOUT_DETAILS_ADDED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    }
    
    @GetMapping("/payout/user/{userId}")
    public ResponseEntity<List<PayoutDetailsResponseDto>> getPayoutDetailsByUser(@PathVariable UUID userId) {
        List<PayoutDetails> payoutDetails = paymentDetailsService.findPayoutDetailsByUserId(userId);
        return ResponseEntity.ok(payoutDetails.stream()
                               .map(PayoutDetailsResponseDto::fromEntity)
                               .toList());
    }
    
    @PutMapping("/payout/{payoutId}")
    public ResponseEntity<PayoutDetailsResponseDto> updatePayoutDetails(
            @PathVariable UUID payoutId,
            @Valid @RequestBody UpdatePayoutDetailsDto updateDto) {
        PayoutDetails payoutDetails = paymentDetailsService.updatePayoutDetails(payoutId, updateDto);
        return ResponseEntity.ok(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    }
    
    @PutMapping("/payout/{payoutId}/set-default")
    public ResponseEntity<PayoutDetailsResponseDto> setActivePayoutMethod(@PathVariable UUID payoutId) {
        PayoutDetails payoutDetails = paymentDetailsService.setAsActive(payoutId);
        return ResponseEntity.ok(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    }
    
    @DeleteMapping("/payout/{payoutId}")
    public ResponseEntity<Void> deletePayoutDetails(@PathVariable UUID payoutId) {
        paymentDetailsService.deletePayoutDetails(payoutId);
        return ResponseEntity.noContent().build();
    }
}
