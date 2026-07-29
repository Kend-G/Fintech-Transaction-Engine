package com.kendry.fintech_api_service.repository;

import com.kendry.fintech_api_service.domain.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BalanceRepository extends JpaRepository<Balance, Long>{
}
