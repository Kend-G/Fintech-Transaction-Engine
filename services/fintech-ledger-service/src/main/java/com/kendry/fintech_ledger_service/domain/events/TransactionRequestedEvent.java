package com.kendry.fintech_ledger_service.domain.events;

import java.math.BigDecimal;

public record TransactionRequestedEvent(
        Long transactionId,
        Long sourceAccountId,
        Long targetAccountId,
        BigDecimal amount,
        String currency,
        String idempotencyKey
) {
}
