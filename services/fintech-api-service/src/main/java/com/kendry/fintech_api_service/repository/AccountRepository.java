package com.kendry.fintech_api_service.repository;

import com.kendry.fintech_api_service.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
