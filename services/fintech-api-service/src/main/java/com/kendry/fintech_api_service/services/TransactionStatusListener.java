package com.kendry.fintech_api_service.services;

import com.kendry.fintech_api_service.domain.enums.TransactionStatus;
import com.kendry.fintech_api_service.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionStatusListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionStatusListener.class);

    private final TransactionRepository transactionRepository;
    private final IdempotencyChecker idempotencyChecker;

    public TransactionStatusListener(TransactionRepository transactionRepository,
                                     IdempotencyChecker idempotencyChecker) {
        this.transactionRepository = transactionRepository;
        this.idempotencyChecker = idempotencyChecker;
    }

    @KafkaListener(topics = "transaction.completed", groupId = "api-service")
    @Transactional
    public void handleCompleted(Long transactionId, Acknowledgment ack) {
        String eventId = transactionId.toString();

        if (idempotencyChecker.isAlreadyProcessed(eventId, "transaction.completed")) {
            ack.acknowledge();
            return;
        }

        log.info("Received completed transaction id {}", transactionId);

        transactionRepository.findById(transactionId).ifPresentOrElse(transaction -> {
            if (transaction.getStatus() != TransactionStatus.COMPLETED) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transactionRepository.save(transaction);
                log.info("Transaction {} marked as completed", transactionId);
            }
        }, () -> log.warn("Transaction {} not found when handling transaction.completed", transactionId));

        ack.acknowledge();
    }

    @KafkaListener(topics = "transaction.failed", groupId = "api-service")
    @Transactional
    public void handleFailed(Long transactionId, Acknowledgment ack) {
        String eventId = transactionId.toString();

        if (idempotencyChecker.isAlreadyProcessed(eventId, "transaction.failed")) {
            ack.acknowledge();
            return;
        }

        log.info("Received failed transaction id {}", transactionId);

        transactionRepository.findById(transactionId).ifPresentOrElse(transaction -> {
            if (transaction.getStatus() != TransactionStatus.FAILED) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                log.info("Transaction {} marked as failed", transactionId);
            }
        }, () -> log.warn("Transaction {} not found when handling transaction.failed", transactionId));

        ack.acknowledge();
    }
}
