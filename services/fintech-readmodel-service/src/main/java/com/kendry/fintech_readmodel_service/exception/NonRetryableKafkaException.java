package com.kendry.fintech_readmodel_service.exception;

public class NonRetryableKafkaException extends RuntimeException {
    public NonRetryableKafkaException(String message) {
        super(message);
    }
}
