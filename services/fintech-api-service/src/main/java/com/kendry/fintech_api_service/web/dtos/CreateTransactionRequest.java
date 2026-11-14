package com.kendry.fintech_api_service.web.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotNull(message = "sourceAccountID is required")
        Long sourceAccountId,

        @NotNull(message = "TargetAccountId is required")
        Long targetAccountId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code")
        String currency,

        @NotBlank(message = "idempotencyKey is required")
        String idempotencyKey
) {

}
