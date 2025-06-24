package com.stockfellow.transactionservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
public class ProcessTransactionRequest {

    @NotNull(message = "Cycle ID is required")
    private UUID cycleId;

    // maybe for failed payments by that user?
    private UUID specificPayerUserId;

    // max retries for a failed payment?
    private Integer maxRetries = 3;

    // "retrying failed transaction"
    private String processingReason;

}
