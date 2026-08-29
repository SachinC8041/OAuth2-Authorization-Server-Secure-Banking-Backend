package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Answers the question "is the caller allowed to read this customer's data?".
 *
 * <p>The account, balance, cards and loans endpoints all take a {@code customerId}
 * query parameter. Authentication alone does not make that safe: without this
 * check any logged-in customer could read any other customer's statements simply
 * by changing the number in the URL. Every endpoint that accepts a customer id
 * runs it through {@link #requireOwnership(Authentication, long)} first.
 */
@Service
@RequiredArgsConstructor
public class CustomerAccessService {

    private final CustomerRepository customerRepository;

    /**
     * Resolves the {@link Customer} row behind the current authentication.
     *
     * @throws AccessDeniedException if there is no authenticated caller, or the
     *                               authenticated login no longer maps to a customer
     */
    public Customer requireAuthenticatedCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated customer for this request");
        }

        return customerRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("No customer record for the authenticated login"));
    }

    /**
     * Confirms that {@code requestedCustomerId} belongs to the caller.
     *
     * <p>The failure message deliberately does not say whether the requested id
     * exists, so the endpoint cannot be used to enumerate customer ids.
     *
     * @throws AccessDeniedException if the id belongs to somebody else
     */
    public void requireOwnership(Authentication authentication, long requestedCustomerId) {
        Customer customer = requireAuthenticatedCustomer(authentication);

        if (customer.getId() == null || customer.getId() != requestedCustomerId) {
            throw new AccessDeniedException("The requested customer id is not available to this user");
        }
    }
}
