package com.kendry.fintech_ledger_service.service;

import com.kendry.fintech_ledger_service.domain.entity.ProcessedEvent;
import com.kendry.fintech_ledger_service.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyChecker {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyChecker.class);

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyChecker(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Returns true if this event was already processed (skip it).
     * Returns false if this is a new event (process it and record it).
     */
    @Transactional
    public boolean isAlreadyProcessed(String eventId, String topic) {
        if (processedEventRepository.existsByEventIdAndTopic(eventId, topic)) {
            log.warn("Duplicate event {} on topic {}, skipping", eventId, topic);
            return true;
        }
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .topic(topic)
                .build());
        return false;
    }
}
