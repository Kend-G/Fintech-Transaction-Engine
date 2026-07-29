package com.kendry.fintech_api_service.web.controllers;

import com.kendry.fintech_api_service.domain.entity.Balance;
import com.kendry.fintech_api_service.services.BalanceService;
import com.kendry.fintech_api_service.web.dtos.BalanceResponse;
import com.kendry.fintech_api_service.web.mappers.BalanceMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {

    private final BalanceService balanceService;
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{accountId}")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        Balance balance = balanceService.getBalance(accountId);
        return BalanceMapper.toResponse(balance);
    }
}
