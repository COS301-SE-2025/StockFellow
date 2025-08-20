package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.User;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusDto {
    
    @NotNull(message = "Status is required")
    private User.UserStatus status;

    // Constructors
    public UpdateUserStatusDto() {}

    // Getters and Setters
    public User.UserStatus getStatus() { return status; }
    public void setStatus(User.UserStatus status) { this.status = status; }
}