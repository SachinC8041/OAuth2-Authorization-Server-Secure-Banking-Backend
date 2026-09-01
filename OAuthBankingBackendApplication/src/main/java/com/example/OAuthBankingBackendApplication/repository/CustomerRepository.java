package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.model.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Looks a customer up by their login name. */
    Optional<Customer> findByEmail(String email);
}
