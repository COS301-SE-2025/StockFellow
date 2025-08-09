package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to initialize card authorization")
public class InitializeCardAuthDto {
    @Schema(description = "User ID who owns the card", required = true)
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @Schema(description = "User's email address", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Schema(description = "Type of payment method", required = true)
    @NotNull(message = "Payment method type is required")
    private String type;

    // Constructors, getters, and setters
    public InitializeCardAuthDto() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
