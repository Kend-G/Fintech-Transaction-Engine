package com.kendry.fintech_ledger_service.service;

import com.kendry.fintech_ledger_service.domain.entity.ProcessedEvent;
import com.kendry.fintech_ledger_service.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyCheckerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private IdempotencyChecker idempotencyChecker;

    private static final String EVENT_ID = "tx-idempotency-key-001";
    private static final String TOPIC = "transaction.requested";

    @BeforeEach
    void setUp() {
        reset(processedEventRepository);
    }

    @Test
    void isAlreadyProcessed_newEvent_returnsFalseAndSaves() {
        when(processedEventRepository.existsByEventIdAndTopic(EVENT_ID, TOPIC)).thenReturn(false);

        boolean result = idempotencyChecker.isAlreadyProcessed(EVENT_ID, TOPIC);

        assertThat(result).isFalse();

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());

        ProcessedEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(EVENT_ID);
        assertThat(saved.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    void isAlreadyProcessed_duplicateEvent_returnsTrueAndDoesNotSave() {
        when(processedEventRepository.existsByEventIdAndTopic(EVENT_ID, TOPIC)).thenReturn(true);

        boolean result = idempotencyChecker.isAlreadyProcessed(EVENT_ID, TOPIC);

        assertThat(result).isTrue();
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void isAlreadyProcessed_sameEventDifferentTopic_treatsAsNew() {
        String otherTopic = "transaction.completed";
        when(processedEventRepository.existsByEventIdAndTopic(EVENT_ID, otherTopic)).thenReturn(false);

        boolean result = idempotencyChecker.isAlreadyProcessed(EVENT_ID, otherTopic);

        assertThat(result).isFalse();
        verify(processedEventRepository).save(any());
    }

    @Test
    void isAlreadyProcessed_differentEventSameTopic_treatsAsNew() {
        String otherEventId = "tx-idempotency-key-002";
        when(processedEventRepository.existsByEventIdAndTopic(otherEventId, TOPIC)).thenReturn(false);

        boolean result = idempotencyChecker.isAlreadyProcessed(otherEventId, TOPIC);

        assertThat(result).isFalse();
        verify(processedEventRepository).save(any());
    }
}
