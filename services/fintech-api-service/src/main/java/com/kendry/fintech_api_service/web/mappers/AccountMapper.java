package com.kendry.fintech_api_service.web.mappers;

import com.kendry.fintech_api_service.domain.entity.Account;
import com.kendry.fintech_api_service.web.dtos.AccountResponse;

public class AccountMapper {
    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerId(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt()
        );

    }
}
