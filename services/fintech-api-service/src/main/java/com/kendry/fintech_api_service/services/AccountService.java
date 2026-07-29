package com.kendry.fintech_api_service.services;

import com.kendry.fintech_api_service.domain.entity.Account;
import com.kendry.fintech_api_service.domain.enums.AccountStatus;
import com.kendry.fintech_api_service.exception.AccountNotFoundException;
import com.kendry.fintech_api_service.repository.AccountRepository;
import com.kendry.fintech_api_service.web.dtos.CreateAccountRequest;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(CreateAccountRequest createAccountRequest) {
        Account account = Account.builder()
                .ownerId(createAccountRequest.ownerId())
                .currency(createAccountRequest.currency())
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(account);

    }

    public Account getAccount(Long id){
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }


}
