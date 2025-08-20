package com.stockfellow.transactionservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionResponse;
import com.stockfellow.transactionservice.integration.dto.PaystackChargeRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferResponse;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionVerificationResponse;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRecipientRequest;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRecipientResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@Service
public class PaystackService {
    private static final Logger logger = LoggerFactory.getLogger(PaystackService.class);
    
    private final RestTemplate restTemplate;
    private final String apiKey;
    private static final String PAYSTACK_BASE_URL = "https://api.paystack.co";

    public PaystackService(
            RestTemplate restTemplate,
            @Value("${paystack.secretKey}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public PaystackTransferResponse initiateTransfer(PaystackTransferRequest request) {
        // try {
        //     HttpHeaders headers = createHeaders();
        //     HttpEntity<PaystackTransferRequest> entity = new HttpEntity<>(request, headers);
            
        //     ResponseEntity<PaystackTransferResponse> response = restTemplate.exchange(
        //         PAYSTACK_BASE_URL + "/transfer",
        //         HttpMethod.POST,
        //         entity,
        //         PaystackTransferResponse.class
        //     );
            
        //     return response.getBody();
        // } catch (Exception e) {
        //     logger.error("Error initiating transfer: ", e);
        //     throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initiate transfer");
        // }
        PaystackTransferResponse.PaystackTransferData data = new PaystackTransferResponse.PaystackTransferData();
        data.setAmount(null);
        data.setId(PAYSTACK_BASE_URL);
        data.setRecipient(null);
        data.setStatus(PAYSTACK_BASE_URL);
        data.setTransferCode(PAYSTACK_BASE_URL);
        

        PaystackTransferResponse response = new PaystackTransferResponse();
        if (!data.getStatus().equals("success")) {
            response.setStatus(true);
            response.setMessage("Transfer has been queued");
        } else {
            response.setStatus(false);
            response.setMessage("");

        }
        response.setData(data);

        return response;
    }

    public PaystackTransactionResponse initializeTransaction(PaystackTransactionRequest request) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<PaystackTransactionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PaystackTransactionResponse> response = restTemplate.exchange(
                PAYSTACK_BASE_URL + "/transaction/initialize",
                HttpMethod.POST,
                entity,
                PaystackTransactionResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error initializing transaction: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize transaction");
        }
    }

    public PaystackTransactionResponse chargeTransaction(PaystackChargeRequest request) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<PaystackChargeRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PaystackTransactionResponse> response = restTemplate.exchange(
                PAYSTACK_BASE_URL + "/transaction/charge_authorization",
                HttpMethod.POST,
                entity,
                PaystackTransactionResponse.class
            );

            // logger.info("Raw Paystack response: {}", rawResponse.getBody());
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error charging transaction: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify transaction");
        }
    }

    public PaystackTransactionVerificationResponse verifyTransaction(String reference) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<PaystackTransactionVerificationResponse> response = restTemplate.exchange(
                PAYSTACK_BASE_URL + "/transaction/verify/" + reference,
                HttpMethod.GET,
                entity,
                PaystackTransactionVerificationResponse.class
            );

            // logger.info("Raw Paystack response: {}", rawResponse.getBody());
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error verifying transaction: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify transaction");
        }
    }

    public PaystackTransferRecipientResponse createTransferRecipient(PaystackTransferRecipientRequest request){
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(request, headers);

            ObjectMapper mapper = new ObjectMapper();
            String jsonRequest = mapper.writeValueAsString(request);
            logger.info("Sending to Paystack: {}", jsonRequest);
            logger.info("Request object: {}", request.toString());

            ResponseEntity<PaystackTransferRecipientResponse> response = restTemplate.exchange(
                PAYSTACK_BASE_URL + "/transferrecipient",
                HttpMethod.POST,
                entity,
                PaystackTransferRecipientResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating transfer recipient: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create transfer recipient");
        }
    } 
}
