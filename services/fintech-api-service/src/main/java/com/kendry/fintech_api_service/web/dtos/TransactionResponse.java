package com.kendry.fintech_api_service.web.dtos;

import com.kendry.fintech_api_service.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long sourceAccountId,
        Long targetAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String idempotencyKey,
        Instant createdAt
) {
}
