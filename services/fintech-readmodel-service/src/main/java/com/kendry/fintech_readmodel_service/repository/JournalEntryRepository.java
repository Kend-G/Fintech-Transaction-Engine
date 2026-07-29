package com.kendry.fintech_readmodel_service.repository;

import com.kendry.fintech_readmodel_service.domain.entities.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByTransactionId(Long transactionId);
}
