package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateCycleStatusDto {
    @NotNull(message = "Status is required")
    private GroupCycle.CycleStatus status;

    public UpdateCycleStatusDto() {}

    public GroupCycle.CycleStatus getStatus() { return status; }
    public void setStatus(GroupCycle.CycleStatus status) { this.status = status; }
}
