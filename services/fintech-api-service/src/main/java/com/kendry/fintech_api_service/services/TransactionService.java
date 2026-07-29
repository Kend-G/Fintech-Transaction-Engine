package com.kendry.fintech_api_service.services;

import com.kendry.fintech_api_service.domain.entity.Transaction;
import com.kendry.fintech_api_service.domain.enums.TransactionStatus;
import com.kendry.fintech_api_service.domain.events.TransactionRequestedEvent;
import com.kendry.fintech_api_service.exception.AccountNotFoundException;
import com.kendry.fintech_api_service.exception.TransactionAlreadyExistsException;
import com.kendry.fintech_api_service.repository.AccountRepository;
import com.kendry.fintech_api_service.repository.TransactionRepository;
import com.kendry.fintech_api_service.web.dtos.CreateTransactionRequest;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

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

    public Transaction createTransaction(CreateTransactionRequest transactionRequest){
        //Idempotency check
        transactionRepository.findByIdempotencyKey(transactionRequest.idempotencyKey())
                .ifPresent(tx ->{
                    throw new TransactionAlreadyExistsException(transactionRequest.idempotencyKey());
                });

        //Validate accounts

        accountRepository.findById(transactionRequest.sourceAccountId())
                .orElseThrow(()->new AccountNotFoundException(transactionRequest.sourceAccountId()));

        accountRepository.findById(transactionRequest.targetAccountId())
                .orElseThrow(()->new AccountNotFoundException(transactionRequest.targetAccountId()));

        //Creating Transaction
        Transaction transaction = Transaction.builder()
                .sourceAccountId(transactionRequest.sourceAccountId())
                .targetAccountId(transactionRequest.targetAccountId())
                .amount(transactionRequest.amount())
                .currency(transactionRequest.currency())
                .idempotencyKey(transactionRequest.idempotencyKey())
                .status(TransactionStatus.PENDING)
                .build();

        transaction = transactionRepository.save(transaction);

        //Publishing event to kafka

        TransactionRequestedEvent event = new TransactionRequestedEvent(
                transaction.getId(),
                transaction.getSourceAccountId(),
                transaction.getTargetAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getIdempotencyKey()

        );
        kafkaTemplate.send("transaction.requested", event);
        return transaction;

    }

    public Transaction getTransaction(Long id){
        return transactionRepository.findById(id)
                .orElseThrow(()->new RuntimeException("transaction not found: " + id));
    }
}
