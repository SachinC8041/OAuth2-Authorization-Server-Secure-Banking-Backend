package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Cards;
import com.example.OAuthBankingBackendApplication.repository.CardsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Card enquiries.
 */
@Service
@RequiredArgsConstructor
public class CardsService {

    private final CardsRepository cardsRepository;
    private final CustomerAccessService customerAccessService;

    /**
     * @return every card issued to the customer
     * @throws org.springframework.security.access.AccessDeniedException if the id
     *                                                                  belongs to somebody else
     */
    @Transactional(readOnly = true)
    public List<Cards> findCardsFor(Authentication authentication, long customerId) {
        customerAccessService.requireOwnership(authentication, customerId);

        return cardsRepository.findByCustomerId(customerId);
    }
}
