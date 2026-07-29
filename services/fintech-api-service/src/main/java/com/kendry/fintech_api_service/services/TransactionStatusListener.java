package com.kendry.fintech_api_service.services;

import com.kendry.fintech_api_service.domain.enums.TransactionStatus;
import com.kendry.fintech_api_service.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static org.apache.kafka.common.requests.FetchMetadata.log;

@Service
public class TransactionStatusListener {

    private final TransactionRepository transactionRepository;
    public TransactionStatusListener(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "transaction.completed", groupId = "api-service")
    public void handleCompleted(Long transactionId){
        log.info("Received completed transaction id {}", transactionId);

        transactionRepository.findById(transactionId).ifPresentOrElse(transaction -> {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("Transaction {} marked as completed", transactionId);
        }, ()-> log.warn("Transaction {} not found when handlin transaction.completed", transactionId));
    }

    @KafkaListener(topics = "transaction.failed", groupId = "api-service")
    public void handleFailed(Long transactionId){
        log.info("Received failed transaction id {}", transactionId);

        transactionRepository.findById(transactionId).ifPresentOrElse(transaction ->{
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
        }, ()-> log.warn("Transaction {} not found when handling transaction.failed", transactionId));
    }
}
