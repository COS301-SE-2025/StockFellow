package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import jakarta.validation.constraints.*;

public class UpdateCycleStatusDto {
    @NotNull(message = "Status is required")
    private GroupCycle.CycleStatus status;

    public UpdateCycleStatusDto() {}

    public GroupCycle.CycleStatus getStatus() { return status; }
    public void setStatus(GroupCycle.CycleStatus status) { this.status = status; }
}
