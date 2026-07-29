package com.kendry.fintech_api_service.web.controllers;


import com.kendry.fintech_api_service.domain.entity.Account;
import com.kendry.fintech_api_service.services.AccountService;
import com.kendry.fintech_api_service.web.dtos.AccountResponse;
import com.kendry.fintech_api_service.web.dtos.CreateAccountRequest;
import com.kendry.fintech_api_service.web.mappers.AccountMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
        Account account = accountService.createAccount(createAccountRequest);
        return AccountMapper.toResponse(account);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        Account account = accountService.getAccount(id);
        return AccountMapper.toResponse(account);
    }

}
