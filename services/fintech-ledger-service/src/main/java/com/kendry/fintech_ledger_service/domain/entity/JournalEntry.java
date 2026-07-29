package com.kendry.fintech_ledger_service.domain.entity;

import com.kendry.fintech_ledger_service.domain.enums.EntryDirection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name ="journal_entries")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;
    private Long accountId;

    @Enumerated(EnumType.STRING)
    private EntryDirection direction;

    private BigDecimal amount;
    private String currency;

    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }


}
