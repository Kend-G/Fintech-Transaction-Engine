package com.kendry.fintech_readmodel_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "processed_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "topic"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String topic;
}
