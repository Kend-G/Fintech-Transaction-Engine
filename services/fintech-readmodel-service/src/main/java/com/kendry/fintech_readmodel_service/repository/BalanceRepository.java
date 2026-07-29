package com.kendry.fintech_readmodel_service.repository;

import com.kendry.fintech_readmodel_service.domain.entities.Balance;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BalanceRepository extends JpaRepository<Balance, Long> {
}
