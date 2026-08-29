package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.AccountTransactions;
import com.example.OAuthBankingBackendApplication.repository.AccountTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Statement enquiries.
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final CustomerAccessService customerAccessService;

    /**
     * @return the customer's statement lines, newest first
     * @throws org.springframework.security.access.AccessDeniedException if the id
     *                                                                  belongs to somebody else
     */
    @Transactional(readOnly = true)
    public List<AccountTransactions> findTransactionsFor(Authentication authentication, long customerId) {
        customerAccessService.requireOwnership(authentication, customerId);

        return accountTransactionRepository.findByCustomerIdOrderByTransactionDtDesc(customerId);
    }
}
