package com.kendry.fintech_api_service.web.mappers;

import com.kendry.fintech_api_service.domain.entity.Transaction;
import com.kendry.fintech_api_service.web.dtos.TransactionResponse;

public class TransactionMapper {
    public static TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getSourceAccountId(),
                transaction.getTargetAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getIdempotencyKey(),
                transaction.getCreatedAt()
        );
    }
}
