package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import jakarta.validation.constraints.*;

public class UpdateCycleStatusDto {
    @NotNull(message = "Status is required")
    private String status;

    public UpdateCycleStatusDto() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
