package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Accounts;
import com.example.OAuthBankingBackendApplication.repository.AccountsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Account enquiries.
 *
 * <p>The ownership check lives here rather than in the controller so that it
 * cannot be skipped by a future caller. A second controller, a scheduled job or
 * a GraphQL resolver that reaches for account data goes through this method and
 * gets the check for free.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountsRepository accountsRepository;
    private final CustomerAccessService customerAccessService;

    /**
     * @param authentication the caller
     * @param customerId     whose account to read; must belong to the caller
     * @return the account, or empty if the customer has none
     * @throws org.springframework.security.access.AccessDeniedException if the id
     *                                                                  belongs to somebody else
     */
    @Transactional(readOnly = true)
    public Optional<Accounts> findAccountFor(Authentication authentication, long customerId) {
        customerAccessService.requireOwnership(authentication, customerId);

        return accountsRepository.findByCustomerId(customerId);
    }
}
