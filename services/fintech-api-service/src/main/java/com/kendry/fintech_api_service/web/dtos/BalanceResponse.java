package com.kendry.fintech_api_service.web.dtos;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(
        Long accountId,
        BigDecimal currentBalance,
        Instant updatedAt
) {
}
