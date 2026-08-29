package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.AccountTransactions;
import com.example.OAuthBankingBackendApplication.service.BalanceService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Transaction history for the signed-in customer.
 */
@RestController
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * @param id the customer id being asked about; must belong to the caller
     * @return statement lines, newest first; an empty list when there are none
     */
    @GetMapping("/balance")
    public ResponseEntity<List<AccountTransactions>> getBalanceDetails(@RequestParam long id,
                                                                       Authentication authentication) {
        return ResponseEntity.ok(balanceService.findTransactionsFor(authentication, id));
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original controller. Same missing ownership check as /account.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.AccountTransactionRepository
     * ----------------------------------------------------------------------
     *
     * private final AccountTransactionRepository accountTransactionsRepository;
     *
     * @GetMapping("/balance")
     * public List<AccountTransactions> getBalanceDetails(@RequestParam long id) {
     *     return accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(id);
     * }
     */
}
