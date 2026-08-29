package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Loans;
import com.example.OAuthBankingBackendApplication.service.LoansService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Loans held by the signed-in customer.
 */
@RestController
@RequiredArgsConstructor
public class LoansController {

    private final LoansService loansService;

    /**
     * @param id the customer id being asked about; must belong to the caller
     * @return loans, most recently started first; an empty list when there are none
     */
    @GetMapping("/loans")
    public ResponseEntity<List<Loans>> getLoanDetails(@RequestParam long id, Authentication authentication) {
        return ResponseEntity.ok(loansService.findLoansFor(authentication, id));
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original controller. Same missing ownership check as /account.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.LoanRepository
     * ----------------------------------------------------------------------
     *
     * private final LoanRepository loanRepository;
     *
     * @GetMapping("/loans")
     * public List<Loans> getLoanDetails(@RequestParam long id) {
     *     return loanRepository.findByCustomerIdOrderByStartDtDesc(id);
     * }
     */
}
