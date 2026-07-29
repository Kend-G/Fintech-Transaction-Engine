package com.kendry.fintech_readmodel_service.domain.entities;

import com.kendry.fintech_readmodel_service.domain.enums.EntryDirection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "Journal_entries")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
}
