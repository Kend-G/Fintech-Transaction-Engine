package com.kendry.fintech_readmodel_service.web.mapper;

import com.kendry.fintech_readmodel_service.domain.entities.Balance;
import com.kendry.fintech_readmodel_service.web.dto.BalanceResponse;

public class BalanceMapper {

    public static BalanceResponse toResponse(Balance balance){
        return new BalanceResponse(
                balance.getAccountId(),
                balance.getCurrentBalance(),
                balance.getUpdatedAt()
        );
    }
}
