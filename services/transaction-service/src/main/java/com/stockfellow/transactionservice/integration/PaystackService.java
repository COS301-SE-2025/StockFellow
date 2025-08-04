package com.stockfellow.transactionservice.integration;

import com.stockfellow.transactionservice.integration.dto.*;
import com.stockfellow.transactionservice.service.TransferService;

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
public class PaystackService {
    private static final Logger logger = LoggerFactory.getLogger(PaystackService.class);

    public PaystackTransferResponse initiateTransfer(PaystackTransferRequest request){
        //TODO: Implement
        return new PaystackTransferResponse();
    }

    public PaystackTransactionResponse initializeTransaction(PaystackTransactionRequest request){
        //TODO: Implement
        return new PaystackTransactionResponse();
    }

    public PaystackTransactionVerificationResponse verifyTransaction(String reference) {
        //TODO: Implement
        return new PaystackTransactionVerificationResponse();
    }
}
