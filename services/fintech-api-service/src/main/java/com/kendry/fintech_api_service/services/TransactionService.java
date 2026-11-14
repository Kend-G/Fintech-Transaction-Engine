package com.kendry.fintech_api_service.services;

import com.kendry.fintech_api_service.domain.entity.Account;
import com.kendry.fintech_api_service.domain.entity.Transaction;
import com.kendry.fintech_api_service.domain.enums.TransactionStatus;
import com.kendry.fintech_api_service.domain.events.TransactionRequestedEvent;
import com.kendry.fintech_api_service.exception.AccountNotFoundException;
import com.kendry.fintech_api_service.exception.TransactionAlreadyExistsException;
import com.kendry.fintech_api_service.repository.AccountRepository;
import com.kendry.fintech_api_service.repository.TransactionRepository;
import com.kendry.fintech_api_service.web.dtos.CreateTransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Transaction createTransaction(CreateTransactionRequest transactionRequest) {
        // Idempotency check
        transactionRepository.findByIdempotencyKey(transactionRequest.idempotencyKey())
                .ifPresent(tx -> {
                    throw new TransactionAlreadyExistsException(transactionRequest.idempotencyKey());
                });

        // Validate accounts
        Account sourceAccount = accountRepository.findById(transactionRequest.sourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException(transactionRequest.sourceAccountId()));

        Account targetAccount = accountRepository.findById(transactionRequest.targetAccountId())
                .orElseThrow(() -> new AccountNotFoundException(transactionRequest.targetAccountId()));

        // Validate no self-transfer
        if (sourceAccount.getId().equals(targetAccount.getId())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        // Validate currency match
        if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
            throw new IllegalArgumentException("Source and target accounts must have the same currency");
        }

        // Creating Transaction
        Transaction transaction = Transaction.builder()
                .sourceAccountId(transactionRequest.sourceAccountId())
                .targetAccountId(transactionRequest.targetAccountId())
                .amount(transactionRequest.amount())
                .currency(transactionRequest.currency())
                .idempotencyKey(transactionRequest.idempotencyKey())
                .status(TransactionStatus.PENDING)
                .build();

        transaction = transactionRepository.save(transaction);

        // Publishing event to Kafka
        TransactionRequestedEvent event = new TransactionRequestedEvent(
                transaction.getId(),
                transaction.getSourceAccountId(),
                transaction.getTargetAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getIdempotencyKey()
        );

        try {
            kafkaTemplate.send("transaction.requested", event)
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to send transaction requested event for transaction {}", transaction.getId(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        return transaction;
    }

    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
