package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.model.PayerDetails;
import com.stockfellow.transactionservice.model.PayoutDetails;
import com.stockfellow.transactionservice.dto.CreatePayerDetailsDto;
import com.stockfellow.transactionservice.dto.CreatePayoutDetailsDto;
import com.stockfellow.transactionservice.dto.PayerDetailsResponseDto;
import com.stockfellow.transactionservice.dto.PayoutDetailsResponseDto;
import com.stockfellow.transactionservice.dto.UpdatePayerDetailsDto;
import com.stockfellow.transactionservice.dto.UpdatePayoutDetailsDto;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.repository.PayerDetailsRepository;
import com.stockfellow.transactionservice.repository.PayoutDetailsRepository;
import com.stockfellow.transactionservice.model.GroupCycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDetailsService.class);

    @Autowired
    private PayoutDetailsRepository payoutDetailsRepository;

    @Autowired
    private PayerDetailsRepository payerDetailsRepository;

    /**
     * ===================================================
     * =                 PayerDetails                    =
     * ===================================================
     */
    public PayerDetails addPayerDetails(CreatePayerDetailsDto createDto){
        logger.info("Adding payer details for user: {}", createDto.getUserId());

        //Validate user exists
        //Validate details
        //Check if these details already exist
        //Check if this needs to be set as active

        PayerDetails pd = new PayerDetails(createDto.getUserId(), createDto.getType(), createDto.getEmail());
        pd.set


    }

    public List<PayerDetails> findPayerDetailsByUserId(UUID userId){
        logger.info("Searching for payer details for user: {}", userId);
    }

    public PayerDetails updatePayerDetails(UUID payerId, UpdatePayerDetailsDto updateDto){
        logger.info("Updating payer detailf for payer details with ID: {}", payerId);

    }

    public PayerDetails deactivateCard(UUID payerId) {
        logger.info("Deactivating card with payer details ID: {}", payerId);
    }

    /**
     * ===================================================
     * =                 PayoutDetails                    =
     * ===================================================
     */
    public PayoutDetails addPayerDetails(CreatePayoutDetailsDto createDto){
        logger.info();

    }

    public List<PayoutDetails> findPayoutDetailsByUserId(UUID userId){
        logger.info();

    }

    public PayoutDetails updatePayoutDetails(UUID payoutId, UpdatePayoutDetailsDto updateDto){
        logger.info();

    }

    public PayoutDetails setAsActive(UUID payoutId) {
        logger.info();

    }

    public PayoutDetails deletePayoutDetails(UUID payoutId) {
        logger.info();
    }

}
