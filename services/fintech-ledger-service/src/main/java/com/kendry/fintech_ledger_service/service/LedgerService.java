package com.kendry.fintech_ledger_service.service;

import com.kendry.fintech_ledger_service.domain.entity.JournalEntry;
import com.kendry.fintech_ledger_service.domain.enums.EntryDirection;
import com.kendry.fintech_ledger_service.domain.events.TransactionRequestedEvent;
import com.kendry.fintech_ledger_service.repository.JournalEntryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static org.apache.kafka.common.requests.FetchMetadata.log;

@Service
public class LedgerService {

    private final JournalEntryRepository journalEntryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LedgerService(JournalEntryRepository journalEntryRepository,
                         KafkaTemplate kafkaTemplate) {
        this.journalEntryRepository = journalEntryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "transaction.requested", groupId = "ledger-service")
    public void processTransaction(TransactionRequestedEvent event){

        try{
            //Creating a debit entry
            JournalEntry debit = JournalEntry.builder()
                    .transactionId(event.transactionId())
                    .accountId(event.sourceAccountId())
                    .direction(EntryDirection.DEBIT)
                    .amount(event.amount())
                    .currency(event.currency())
                    .build();

            //Creating Credit
            JournalEntry credit = JournalEntry.builder()
                    .transactionId(event.transactionId())
                    .accountId(event.targetAccountId())
                    .direction(EntryDirection.CREDIT)
                    .amount(event.amount())
                    .currency(event.currency())
                    .build();

            journalEntryRepository.save(debit);
            journalEntryRepository.save(credit);

            //Publishing completed event
            kafkaTemplate.send("transaction.completed",event.transactionId());
        } catch(Exception e){
            log.error("Failed to process transaction {}", event.transactionId(), e);
            kafkaTemplate.send("transaction.failed",event.transactionId());
        }

    }
}
