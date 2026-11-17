package com.kendry.fintech_ledger_service.service;

import com.kendry.fintech_ledger_service.domain.entity.JournalEntry;
import com.kendry.fintech_ledger_service.domain.enums.EntryDirection;
import com.kendry.fintech_ledger_service.domain.events.TransactionRequestedEvent;
import com.kendry.fintech_ledger_service.repository.JournalEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final JournalEntryRepository journalEntryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdempotencyChecker idempotencyChecker;

    public LedgerService(JournalEntryRepository journalEntryRepository,
                         KafkaTemplate<String, Object> kafkaTemplate,
                         IdempotencyChecker idempotencyChecker) {
        this.journalEntryRepository = journalEntryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.idempotencyChecker = idempotencyChecker;
    }

    @KafkaListener(topics = "transaction.requested", groupId = "ledger-service")
    @Transactional
    public void processTransaction(TransactionRequestedEvent event, Acknowledgment ack) {
        String eventId = event.idempotencyKey() != null ? event.idempotencyKey() : event.transactionId().toString();

        if (idempotencyChecker.isAlreadyProcessed(eventId, "transaction.requested")) {
            ack.acknowledge();
            return;
        }

        try {
            // Creating a debit entry
            JournalEntry debit = JournalEntry.builder()
                    .transactionId(event.transactionId())
                    .accountId(event.sourceAccountId())
                    .direction(EntryDirection.DEBIT)
                    .amount(event.amount())
                    .currency(event.currency())
                    .build();

            // Creating Credit
            JournalEntry credit = JournalEntry.builder()
                    .transactionId(event.transactionId())
                    .accountId(event.targetAccountId())
                    .direction(EntryDirection.CREDIT)
                    .amount(event.amount())
                    .currency(event.currency())
                    .build();

            journalEntryRepository.save(debit);
            journalEntryRepository.save(credit);

            // Publishing completed event
            kafkaTemplate.send("transaction.completed", event.transactionId())
                    .get(10, TimeUnit.SECONDS);

            ack.acknowledge();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to send completed event for transaction {}", event.transactionId(), e);
            try {
                kafkaTemplate.send("transaction.failed", event.transactionId())
                        .get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                log.error("Failed to send failed event for transaction {}", event.transactionId(), ex);
            }
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Failed to process transaction {}", event.transactionId(), e);
            try {
                kafkaTemplate.send("transaction.failed", event.transactionId())
                        .get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                log.error("Failed to send failed event for transaction {}", event.transactionId(), ex);
            }
        }
    }
}
