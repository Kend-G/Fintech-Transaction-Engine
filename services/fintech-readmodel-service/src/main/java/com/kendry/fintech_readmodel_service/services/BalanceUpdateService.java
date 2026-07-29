package com.kendry.fintech_readmodel_service.services;

import com.kendry.fintech_readmodel_service.domain.entities.Balance;
import com.kendry.fintech_readmodel_service.domain.entities.JournalEntry;
import com.kendry.fintech_readmodel_service.domain.enums.EntryDirection;
import com.kendry.fintech_readmodel_service.events.TransactionCompletedEvent;
import com.kendry.fintech_readmodel_service.repository.BalanceRepository;
import com.kendry.fintech_readmodel_service.repository.JournalEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(BalanceUpdateService.class);

    private final BalanceRepository balanceRepository;
    private final JournalEntryRepository journalEntryRepository;


    public BalanceUpdateService(BalanceRepository balanceRepository,
                                JournalEntryRepository journalEntryRepository) {
        this.balanceRepository = balanceRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @KafkaListener(topics = "transaction.completed", groupId = "readmodel-service")
    public void updateBalance(Long transactionId) {

        List<JournalEntry> entries = journalEntryRepository.findByTransactionId(transactionId);

        for (JournalEntry entry : entries) {

            Balance balance = balanceRepository.findById((entry.getAccountId()))
                    .orElse(new Balance(entry.getAccountId(), BigDecimal.ZERO, null));

            if(entry.getDirection() == EntryDirection.DEBIT){
                balance.setCurrentBalance(balance.getCurrentBalance().subtract(entry.getAmount()));

            }
            else{
                balance.setCurrentBalance(balance.getCurrentBalance().add(entry.getAmount()));
            }
         balanceRepository.save(balance);
        }

        log.info("update balances for completes transaction {}", "transactionId");

    }
}
