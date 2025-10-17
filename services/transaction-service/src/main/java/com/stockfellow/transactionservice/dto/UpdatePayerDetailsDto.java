package com.stockfellow.transactionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UpdatePayerDetailsDto {
    @Schema(description = "Whether this payment method has been successfully authenticated")
    private Boolean isAuthenticated;
    
    @Schema(description = "Whether this is the user's active/primary payment method")
    private Boolean isActive;

    public UpdatePayerDetailsDto() {}

    // Getters and Setters
    public Boolean getIsAuthenticated() { return isAuthenticated; }
    public void setIsAuthenticated(Boolean isAuthenticated) { this.isAuthenticated = isAuthenticated; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}