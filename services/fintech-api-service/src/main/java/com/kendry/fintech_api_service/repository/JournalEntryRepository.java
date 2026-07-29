package com.kendry.fintech_api_service.repository;

import com.kendry.fintech_api_service.domain.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
