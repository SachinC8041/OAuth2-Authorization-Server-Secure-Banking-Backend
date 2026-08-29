package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Customer registration and lookup.
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerAccessService customerAccessService;

    /**
     * Hashes the submitted password and stores the customer.
     *
     * <p>No authority is granted here. A newly registered customer can log in but
     * cannot read anything until a row exists for them in the {@code authorities}
     * table - see the roadmap in the README.
     *
     * @throws org.springframework.dao.DataIntegrityViolationException if the
     *                                                                e-mail is already taken
     */
    @Transactional
    public Customer register(Customer customer) {
        customer.setPwd(passwordEncoder.encode(customer.getPwd()));
        customer.setCreateDt(new Date(System.currentTimeMillis()));

        return customerRepository.save(customer);
    }

    /**
     * @return the customer behind the current authentication
     */
    @Transactional(readOnly = true)
    public Customer findAuthenticatedCustomer(Authentication authentication) {
        return customerAccessService.requireAuthenticatedCustomer(authentication);
    }
}
