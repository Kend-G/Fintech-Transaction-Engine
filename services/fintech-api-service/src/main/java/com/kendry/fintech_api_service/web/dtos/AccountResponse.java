package com.kendry.fintech_api_service.web.dtos;

import com.kendry.fintech_api_service.domain.enums.AccountStatus;

import java.time.Instant;

public record AccountResponse(
        Long id,
        String ownerId,
        String currency,
        AccountStatus status,
        Instant createdAt
) {
}
