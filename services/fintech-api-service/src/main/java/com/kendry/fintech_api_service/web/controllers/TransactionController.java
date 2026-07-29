package com.kendry.fintech_api_service.web.controllers;


import com.kendry.fintech_api_service.domain.entity.Transaction;
import com.kendry.fintech_api_service.services.TransactionService;
import com.kendry.fintech_api_service.web.dtos.CreateTransactionRequest;
import com.kendry.fintech_api_service.web.dtos.TransactionResponse;
import com.kendry.fintech_api_service.web.mappers.TransactionMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

///
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @PostMapping
    public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest) {
        Transaction transaction = transactionService.createTransaction(createTransactionRequest);
        return TransactionMapper.toResponse(transaction);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransaction(id);
        return TransactionMapper.toResponse(transaction);
    }

}
