package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Loans;
import com.example.OAuthBankingBackendApplication.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loan enquiries.
 */
@Service
@RequiredArgsConstructor
public class LoansService {

    private final LoanRepository loanRepository;
    private final CustomerAccessService customerAccessService;

    /**
     * @return the customer's loans, most recently started first
     * @throws org.springframework.security.access.AccessDeniedException if the id
     *                                                                  belongs to somebody else
     */
    @Transactional(readOnly = true)
    public List<Loans> findLoansFor(Authentication authentication, long customerId) {
        customerAccessService.requireOwnership(authentication, customerId);

        return loanRepository.findByCustomerIdOrderByStartDtDesc(customerId);
    }
}
