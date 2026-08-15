package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.AccountTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransactions, String> {
 List<AccountTransactions> findByCustomerIdOrderByTransactionDtDesc(long customerId);
}
