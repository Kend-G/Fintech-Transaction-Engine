package com.kendry.fintech_api_service.web.mappers;


import com.kendry.fintech_api_service.domain.entity.Balance;
import com.kendry.fintech_api_service.web.dtos.BalanceResponse;

public class BalanceMapper {

    public static BalanceResponse toResponse(Balance balance){

        return new BalanceResponse(
        balance.getAccountId(),
        balance.getCurrentBalance(),
        balance.getUpdatedAt()
        );

    }


}
