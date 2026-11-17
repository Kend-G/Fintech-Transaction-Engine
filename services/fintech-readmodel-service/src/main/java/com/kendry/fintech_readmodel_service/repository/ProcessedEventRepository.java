package com.kendry.fintech_readmodel_service.repository;

import com.kendry.fintech_readmodel_service.domain.entities.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventIdAndTopic(String eventId, String topic);
}
