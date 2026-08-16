package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.Accounts;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository extends CrudRepository<Accounts, Long> {

    // NOTE: returns a single row. If a customer ever has more than one account
    // this throws IncorrectResultSizeDataAccessException - switch the return
    // type to List<Accounts> if multiple accounts per customer are possible.
    Accounts findByCustomerId(long customerId);
}
