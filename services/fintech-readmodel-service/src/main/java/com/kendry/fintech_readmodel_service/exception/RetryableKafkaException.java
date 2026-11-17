package com.kendry.fintech_readmodel_service.exception;

public class RetryableKafkaException extends RuntimeException {
    public RetryableKafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
