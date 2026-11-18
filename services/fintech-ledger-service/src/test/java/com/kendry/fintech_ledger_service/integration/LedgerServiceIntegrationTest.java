package com.kendry.fintech_ledger_service.integration;

import com.kendry.fintech_ledger_service.domain.entity.JournalEntry;
import com.kendry.fintech_ledger_service.domain.entity.ProcessedEvent;
import com.kendry.fintech_ledger_service.domain.enums.EntryDirection;
import com.kendry.fintech_ledger_service.domain.events.TransactionRequestedEvent;
import com.kendry.fintech_ledger_service.repository.JournalEntryRepository;
import com.kendry.fintech_ledger_service.repository.ProcessedEventRepository;
import com.kendry.fintech_ledger_service.service.IdempotencyChecker;
import com.kendry.fintech_ledger_service.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class LedgerServiceIntegrationTest {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private IdempotencyChecker idempotencyChecker;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private Acknowledgment acknowledgment;

    @Autowired
    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        journalEntryRepository.deleteAll();
        processedEventRepository.deleteAll();
        reset(kafkaTemplate, acknowledgment);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void processTransaction_persistsDebitAndCreditToDatabase() {
        TransactionRequestedEvent event = new TransactionRequestedEvent(
                1L, 10L, 20L, new BigDecimal("200.00"), "USD", "tx-int-001"
        );

        ledgerService.processTransaction(event, acknowledgment);

        List<JournalEntry> entries = journalEntryRepository.findAll();
        assertThat(entries).hasSize(2);

        JournalEntry debit = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.DEBIT)
                .findFirst()
                .orElseThrow();

        JournalEntry credit = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.CREDIT)
                .findFirst()
                .orElseThrow();

        assertThat(debit.getAccountId()).isEqualTo(10L);
        assertThat(debit.getAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(debit.getCurrency()).isEqualTo("USD");
        assertThat(debit.getCreatedAt()).isNotNull();

        assertThat(credit.getAccountId()).isEqualTo(20L);
        assertThat(credit.getAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(credit.getCurrency()).isEqualTo("USD");
        assertThat(credit.getCreatedAt()).isNotNull();
    }

    @Test
    void processTransaction_recordsProcessedEventInDatabase() {
        TransactionRequestedEvent event = new TransactionRequestedEvent(
                2L, 10L, 20L, new BigDecimal("50.00"), "EUR", "tx-int-002"
        );

        ledgerService.processTransaction(event, acknowledgment);

        Optional<ProcessedEvent> processedEvent = processedEventRepository
                .findByEventIdAndTopic("tx-int-002", "transaction.requested");

        assertThat(processedEvent).isPresent();
        assertThat(processedEvent.get().getEventId()).isEqualTo("tx-int-002");
        assertThat(processedEvent.get().getTopic()).isEqualTo("transaction.requested");
    }

    @Test
    void processTransaction_secondCallWithSameIdempotencyKey_doesNotCreateDuplicateEntries() {
        TransactionRequestedEvent event = new TransactionRequestedEvent(
                3L, 10L, 20L, new BigDecimal("75.50"), "GBP", "tx-int-003"
        );

        ledgerService.processTransaction(event, acknowledgment);
        assertThat(journalEntryRepository.count()).isEqualTo(2);
        assertThat(processedEventRepository.count()).isEqualTo(1);

        ledgerService.processTransaction(event, acknowledgment);
        assertThat(journalEntryRepository.count()).isEqualTo(2);
        assertThat(processedEventRepository.count()).isEqualTo(1);
    }

    @Test
    void processTransaction_multipleTransactions_accumulateEntriesCorrectly() {
        TransactionRequestedEvent event1 = new TransactionRequestedEvent(
                10L, 100L, 200L, new BigDecimal("100.00"), "USD", "tx-int-multi-001"
        );
        TransactionRequestedEvent event2 = new TransactionRequestedEvent(
                11L, 100L, 200L, new BigDecimal("50.00"), "USD", "tx-int-multi-002"
        );

        ledgerService.processTransaction(event1, acknowledgment);
        ledgerService.processTransaction(event2, acknowledgment);

        List<JournalEntry> entries = journalEntryRepository.findAll();
        assertThat(entries).hasSize(4);

        BigDecimal totalDebits = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.DEBIT)
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.CREDIT)
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalDebits).isEqualTo(new BigDecimal("150.00"));
        assertThat(totalCredits).isEqualTo(new BigDecimal("150.00"));
        assertThat(totalDebits).isEqualTo(totalCredits);
    }

    @Test
    void processTransaction_withoutIdempotencyKey_usesTransactionId() {
        TransactionRequestedEvent event = new TransactionRequestedEvent(
                99L, 10L, 20L, new BigDecimal("30.00"), "JPY", null
        );

        ledgerService.processTransaction(event, acknowledgment);

        Optional<ProcessedEvent> processedEvent = processedEventRepository
                .findByEventIdAndTopic("99", "transaction.requested");

        assertThat(processedEvent).isPresent();
        assertThat(journalEntryRepository.count()).isEqualTo(2);
    }
}
