package com.stockfellow.transactionservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Data
public class CreateMandateRequest {

    @NotNull(message = "Payer user ID is required")
    private UUID payerUserId;

    @NotNull(message = "Group ID is required")
    private UUID groupId;

    @NotNull(message = "Payment method ID is required")
    private UUID paymentMethodId;

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String documentReference;

}
