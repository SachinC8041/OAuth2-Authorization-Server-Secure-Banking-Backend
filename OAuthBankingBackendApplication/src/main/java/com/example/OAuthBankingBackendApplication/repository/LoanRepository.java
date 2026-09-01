package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.model.Loans;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends CrudRepository<Loans, Long> {

    /** Loans held by a customer, most recently started first. */
    List<Loans> findByCustomerIdOrderByStartDtDesc(long customerId);
}
