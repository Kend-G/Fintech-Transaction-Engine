package com.kendry.fintech_ledger_service.exception;

public class NonRetryableKafkaException extends RuntimeException {
    public NonRetryableKafkaException(String message) {
        super(message);
    }
}
