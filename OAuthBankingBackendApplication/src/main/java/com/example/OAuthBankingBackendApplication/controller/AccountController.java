package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Accounts;
import com.example.OAuthBankingBackendApplication.repository.AccountsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountsRepository accountsRepository;

    @GetMapping("/account")
    public Accounts getAccountDetails(@RequestParam long id) {
        return accountsRepository.findByCustomerId(id);
    }
}
