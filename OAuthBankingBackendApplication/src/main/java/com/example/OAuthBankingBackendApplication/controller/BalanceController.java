package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.AccountTransactions;
import com.example.OAuthBankingBackendApplication.repository.AccountTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BalanceController {

    private final AccountTransactionRepository accountTransactionsRepository;

    @GetMapping("/balance")
    public List<AccountTransactions> getBalanceDetails(@RequestParam long id) {
        return accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(id);
    }
}
