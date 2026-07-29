package com.kendry.fintech_api_service.services;

import com.kendry.fintech_api_service.domain.entity.Balance;
import com.kendry.fintech_api_service.repository.BalanceRepository;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public Balance getBalance(Long accountId){

        return balanceRepository.findById((accountId))
                .orElse(new Balance(accountId, null, null));

    }
}
