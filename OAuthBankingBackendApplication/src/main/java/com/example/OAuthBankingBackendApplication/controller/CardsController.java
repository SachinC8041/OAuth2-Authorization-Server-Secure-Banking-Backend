package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Cards;
import com.example.OAuthBankingBackendApplication.service.CardsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cards issued to the signed-in customer.
 */
@RestController
@RequiredArgsConstructor
public class CardsController {

    private final CardsService cardsService;

    /**
     * @param id the customer id being asked about; must belong to the caller
     * @return every card held by the customer; an empty list when there are none
     */
    @GetMapping("/cards")
    public ResponseEntity<List<Cards>> getCardDetails(@RequestParam long id, Authentication authentication) {
        return ResponseEntity.ok(cardsService.findCardsFor(authentication, id));
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original controller. Same missing ownership check as /account.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.CardsRepository
     * ----------------------------------------------------------------------
     *
     * private final CardsRepository cardsRepository;
     *
     * @GetMapping("/cards")
     * public List<Cards> getCardDetails(@RequestParam long id) {
     *     return cardsRepository.findByCustomerId(id);
     * }
     */
}
