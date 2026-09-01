package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.model.Cards;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardsRepository extends CrudRepository<Cards, Long> {

    /** Every card issued to a customer. */
    List<Cards> findByCustomerId(long customerId);
}
