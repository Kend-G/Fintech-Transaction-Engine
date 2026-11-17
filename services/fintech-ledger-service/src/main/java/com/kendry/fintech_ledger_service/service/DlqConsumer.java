package com.kendry.fintech_ledger_service.service;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    @KafkaListener(topics = "transaction.requested.dlq", groupId = "dlq-inspector")
    public void consumeRequestedDlq(Object message, Headers headers) {
        String errorMessage = firstHeader(headers, "kafka_dlt-exception-message");
        log.error("DLQ (transaction.requested) — Error: {}, Payload: {}", errorMessage, message);
    }

    private String firstHeader(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        return header != null ? new String(header.value()) : "unknown";
    }
}
