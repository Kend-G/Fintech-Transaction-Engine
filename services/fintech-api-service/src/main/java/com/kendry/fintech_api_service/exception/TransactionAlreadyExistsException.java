package com.kendry.fintech_api_service.exception;

public class TransactionAlreadyExistsException extends RuntimeException {
    public TransactionAlreadyExistsException(String idempotencyKey) {
        super("Transaction with idempotencyKey already exits: " + idempotencyKey);
    }
}
