package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Accounts;
import com.example.OAuthBankingBackendApplication.service.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account details for the signed-in customer.
 */
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * @param id the customer id being asked about; must belong to the caller
     * @return {@code 200} with the account, {@code 403} if the id belongs to
     *         somebody else, {@code 404} if the customer has no account
     */
    @GetMapping("/account")
    public ResponseEntity<Accounts> getAccountDetails(@RequestParam long id, Authentication authentication) {
        return accountService.findAccountFor(authentication, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original controller: repository injected straight in, no service
     *     layer and no ownership check.
     *
     *     The security hole to remember: authentication only proves who the
     *     caller is. Nothing here checked that the id in the query string was
     *     theirs, so any logged-in customer could read any other customer's
     *     account by changing one number in the URL. That is an insecure direct
     *     object reference, and the authorizeHttpRequests rules cannot catch it
     *     because the request IS from a valid ROLE_USER.
     *
     *     Also note the return type: repository.findByCustomerId returned the
     *     entity or null, and a null body serialises as an empty 200 rather than
     *     a 404.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.AccountsRepository
     * ----------------------------------------------------------------------
     *
     * private final AccountsRepository accountsRepository;
     *
     * @GetMapping("/account")
     * public Accounts getAccountDetails(@RequestParam long id) {
     *     return accountsRepository.findByCustomerId(id);
     * }
     */
}
