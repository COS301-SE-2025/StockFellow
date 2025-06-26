package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateMandateRequest;
import com.stockfellow.transactionservice.dto.MandateResponse;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.repository.MandateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MandateService {
    
    private static final Logger logger = LoggerFactory.getLogger(MandateService.class);
    private final MandateRepository mandateRepository;

    public MandateService(MandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Transactional
    public Mandate createMandate(CreateMandateRequest request) {
        logger.info("Creating mandate for user {} in group {}", request.getPayerUserId(), request.getGroupId());
        
        // Validate request
        validateCreateMandateRequest(request);
        
        // Check if mandate already exists for this user and group
        if (mandateRepository.existsByPayerUserIdAndGroupId(request.getPayerUserId(), request.getGroupId())) {
            throw new IllegalStateException("Mandate already exists for user " + request.getPayerUserId() + " in group " + request.getGroupId());
        }
        
        // Create mandate entity
        Mandate mandate = new Mandate();
        mandate.setPayerUserId(request.getPayerUserId());
        mandate.setGroupId(request.getGroupId());
        mandate.setPaymentMethodId(request.getPaymentMethodId());
        mandate.setStatus("ACTIVE"); // or whatever your default status is
        mandate.setSignedDate(LocalDateTime.now());
        mandate.setDocumentReference(request.getDocumentReference());
        mandate.setIpAddress(request.getIpAddress());
        
        // Save mandate
        Mandate savedMandate = mandateRepository.save(mandate);
        
        logger.info("Successfully created mandate with ID: {}", savedMandate.getMandateId());
        return savedMandate;
    }
    
    private void validateCreateMandateRequest(CreateMandateRequest request) {
        if (request.getPayerUserId() == null) {
            throw new IllegalArgumentException("Payer user ID cannot be null");
        }
        if (request.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        if (request.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Payment method ID cannot be null");
        }
        if (request.getIpAddress() == null || request.getIpAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
    }
    
    public List<MandateResponse> getAllMandates() {
        logger.info("Fetching mandates");
        List<Mandate> mandates = mandateRepository.findAll();
        return mandates.stream()
                .map(MandateResponse::from)
                .collect(Collectors.toList());
    }

    public Mandate getMandateById(UUID mandateId) {
        return mandateRepository.findById(mandateId)
            .orElseThrow(() -> new IllegalArgumentException("Mandate not found with ID: " + mandateId));
    }
    
    public void deactivateMandate(UUID mandateId) {
        Mandate mandate = getMandateById(mandateId);
        mandate.setStatus("INACTIVE");
        mandateRepository.save(mandate);
        logger.info("Deactivated mandate with ID: {}", mandateId);
    }
    
    public List<MandateResponse> getMandatesByGroup(UUID groupId) {
        logger.info("Fetching mandates for group: {}", groupId);
        List<Mandate> mandates = mandateRepository.findByGroupId(groupId);
        return mandates.stream()
                .map(MandateResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<MandateResponse> getMandatesByStatus(String status) {
        logger.info("Fetching mandates with status: {}", status);
        List<Mandate> mandates = mandateRepository.findByStatus(status);
        return mandates.stream()
                .map(MandateResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<MandateResponse> getActiveMandatesByGroup(UUID groupId) {
        logger.info("Fetching active mandates for group: {}", groupId);
        List<Mandate> mandates = mandateRepository.findActiveMandatesByGroupId(groupId);
        return mandates.stream()
                .map(MandateResponse::from)
                .collect(Collectors.toList());
    }
}