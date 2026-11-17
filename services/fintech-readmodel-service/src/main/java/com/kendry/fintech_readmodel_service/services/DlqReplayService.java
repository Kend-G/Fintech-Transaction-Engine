package com.kendry.fintech_readmodel_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DlqReplayService {

    private static final Logger log = LoggerFactory.getLogger(DlqReplayService.class);

    /**
     * The readmodel-service is a read-only consumer; it does not produce to Kafka.
     * To replay a DLQ message, use the Kafka CLI or the api-service's DlqReplayService.
     */
    public void replayMessage(String originalTopic, Object message) {
        log.warn("Readmodel-service cannot replay messages directly. "
                + "Use Kafka CLI to replay to topic {}: {}", originalTopic, message);
    }
}
