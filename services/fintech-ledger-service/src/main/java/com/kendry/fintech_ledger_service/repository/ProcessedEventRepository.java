package com.kendry.fintech_ledger_service.repository;

import com.kendry.fintech_ledger_service.domain.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventIdAndTopic(String eventId, String topic);

    Optional<ProcessedEvent> findByEventIdAndTopic(String eventId, String topic);
}
