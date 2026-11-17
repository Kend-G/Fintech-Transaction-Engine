package com.kendry.fintech_api_service.exception;

public class NonRetryableKafkaException extends RuntimeException {
    public NonRetryableKafkaException(String message) {
        super(message);
    }
}
