package com.kendry.fintech_api_service.web;

import com.kendry.fintech_api_service.exception.AccountNotFoundException;
import com.kendry.fintech_api_service.exception.TransactionAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleAccountNotFound(AccountNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
    }

    @ExceptionHandler(TransactionAlreadyExistsException.class)
    public ResponseEntity<?> handleDuplicateTransaction(TransactionAlreadyExistsException ex){

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex){
        log.error("Unhandled exception while processing request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("Internal Server Error"));
    }

    private Map<String, Object> error (String message){
        return Map.of(
                "timeStamp", Instant.now(),
                "error", message
        );
    }
}
