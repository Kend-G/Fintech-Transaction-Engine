package com.kendry.fintech_readmodel_service.web.controller;

import com.kendry.fintech_readmodel_service.domain.entities.Balance;
import com.kendry.fintech_readmodel_service.repository.BalanceRepository;
import com.kendry.fintech_readmodel_service.web.dto.BalanceResponse;
import com.kendry.fintech_readmodel_service.web.mapper.BalanceMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {

    private final BalanceRepository balanceRepository;

    public BalanceController(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }


    @GetMapping("/{accountId}")
    public BalanceResponse getBalance(@PathVariable Long accountId){

        Balance balance = balanceRepository.findById(accountId)
                .orElse(new Balance(accountId, BigDecimal.ZERO, null));

        return BalanceMapper.toResponse(balance);
    }


}
