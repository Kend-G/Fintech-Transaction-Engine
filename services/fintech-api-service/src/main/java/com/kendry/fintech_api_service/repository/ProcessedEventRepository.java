package com.kendry.fintech_api_service.repository;

import com.kendry.fintech_api_service.domain.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventIdAndTopic(String eventId, String topic);
}
