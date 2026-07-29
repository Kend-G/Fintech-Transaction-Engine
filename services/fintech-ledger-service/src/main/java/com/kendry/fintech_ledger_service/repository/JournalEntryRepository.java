package com.kendry.fintech_ledger_service.repository;

import com.kendry.fintech_ledger_service.domain.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
