package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Loans;
import com.example.OAuthBankingBackendApplication.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoansController {

    private final LoanRepository loanRepository;

    @GetMapping("/loans")
    public List<Loans> getLoanDetails(@RequestParam long id) {
        return loanRepository.findByCustomerIdOrderByStartDtDesc(id);
    }
}
