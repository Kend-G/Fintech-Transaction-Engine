package com.kendry.fintech_readmodel_service.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(
        Long accountId,
        BigDecimal currentBalance,
        Instant updatedAt
) {
}
