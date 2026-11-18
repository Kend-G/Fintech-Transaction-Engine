package com.kendry.fintech_ledger_service.service;

import com.kendry.fintech_ledger_service.domain.entity.JournalEntry;
import com.kendry.fintech_ledger_service.domain.enums.EntryDirection;
import com.kendry.fintech_ledger_service.domain.events.TransactionRequestedEvent;
import com.kendry.fintech_ledger_service.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private IdempotencyChecker idempotencyChecker;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private LedgerService ledgerService;

    private TransactionRequestedEvent event;

    @BeforeEach
    void setUp() {
        reset(journalEntryRepository, kafkaTemplate, idempotencyChecker, acknowledgment);

        event = new TransactionRequestedEvent(
                1L,   // transactionId
                10L,  // sourceAccountId
                20L,  // targetAccountId
                new BigDecimal("150.75"),  // amount
                "USD",  // currency
                "tx-key-001"  // idempotencyKey
        );

        when(idempotencyChecker.isAlreadyProcessed(anyString(), anyString())).thenReturn(false);
        lenient().when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void processTransaction_createsDebitAndCreditEntries() {
        ledgerService.processTransaction(event, acknowledgment);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository, times(2)).save(captor.capture());

        List<JournalEntry> entries = captor.getAllValues();
        assertThat(entries).hasSize(2);

        JournalEntry debit = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.DEBIT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No DEBIT entry found"));

        JournalEntry credit = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No CREDIT entry found"));

        assertThat(debit.getAccountId()).isEqualTo(event.sourceAccountId());
        assertThat(debit.getAmount()).isEqualTo(event.amount());
        assertThat(debit.getCurrency()).isEqualTo(event.currency());
        assertThat(debit.getTransactionId()).isEqualTo(event.transactionId());

        assertThat(credit.getAccountId()).isEqualTo(event.targetAccountId());
        assertThat(credit.getAmount()).isEqualTo(event.amount());
        assertThat(credit.getCurrency()).isEqualTo(event.currency());
        assertThat(credit.getTransactionId()).isEqualTo(event.transactionId());
    }

    @Test
    void processTransaction_debitAndCreditAmountsMatch() {
        ledgerService.processTransaction(event, acknowledgment);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository, times(2)).save(captor.capture());

        List<JournalEntry> entries = captor.getAllValues();
        BigDecimal debitAmount = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.DEBIT)
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditAmount = entries.stream()
                .filter(e -> e.getDirection() == EntryDirection.CREDIT)
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(debitAmount).isEqualTo(creditAmount);
    }

    @Test
    void processTransaction_publishesCompletedEvent() {
        ledgerService.processTransaction(event, acknowledgment);

        verify(kafkaTemplate).send("transaction.completed", event.transactionId());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processTransaction_duplicateEvent_skipsProcessing() {
        when(idempotencyChecker.isAlreadyProcessed(event.idempotencyKey(), "transaction.requested"))
                .thenReturn(true);

        ledgerService.processTransaction(event, acknowledgment);

        verify(journalEntryRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processTransaction_fallsBackToTransactionIdWhenNoIdempotencyKey() {
        TransactionRequestedEvent eventWithoutKey = new TransactionRequestedEvent(
                5L, 10L, 20L, new BigDecimal("100.00"), "EUR", null
        );

        when(idempotencyChecker.isAlreadyProcessed("5", "transaction.requested")).thenReturn(false);

        ledgerService.processTransaction(eventWithoutKey, acknowledgment);

        verify(journalEntryRepository, times(2)).save(any());
        verify(kafkaTemplate).send("transaction.completed", 5L);
    }

    @Test
    void processTransaction_publishesFailedEventOnKafkaSendError() {
        when(kafkaTemplate.send(anyString(), any()))
                .thenThrow(new RuntimeException("Kafka broker unavailable"));

        try {
            ledgerService.processTransaction(event, acknowledgment);
        } catch (RuntimeException ignored) {
        }

        verify(journalEntryRepository, times(2)).save(any());
        verify(kafkaTemplate).send("transaction.failed", event.transactionId());
    }
}
