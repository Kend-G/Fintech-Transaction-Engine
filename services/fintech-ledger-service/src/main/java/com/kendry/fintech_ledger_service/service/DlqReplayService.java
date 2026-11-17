package com.kendry.fintech_ledger_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DlqReplayService {

    private static final Logger log = LoggerFactory.getLogger(DlqReplayService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DlqReplayService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Replay a message from the DLQ back to the original topic.
     * Call this after fixing the root cause of the failure.
     */
    public void replayMessage(String originalTopic, Object message) {
        try {
            kafkaTemplate.send(originalTopic, message).get(10, TimeUnit.SECONDS);
            log.info("Replayed message to topic {}: {}", originalTopic, message);
        } catch (Exception e) {
            log.error("Failed to replay message to topic {}", originalTopic, e);
        }
    }
}
