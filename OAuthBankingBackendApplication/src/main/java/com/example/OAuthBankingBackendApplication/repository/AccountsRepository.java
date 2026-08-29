package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.Accounts;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends CrudRepository<Accounts, Long> {

    /**
     * Returns the account belonging to a customer.
     *
     * <p>Wrapped in {@link Optional} so a customer with no account is an ordinary
     * empty result rather than a {@code null} the caller has to remember to check.
     *
     * <p>This still assumes at most one account per customer; a second row makes
     * Spring Data throw {@code IncorrectResultSizeDataAccessException}. Widen the
     * return type to {@code List<Accounts>} when multiple accounts become possible.
     */
    Optional<Accounts> findByCustomerId(long customerId);


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original signature, returning the entity directly.
     *
     *     Spring Data returns null when nothing matches, and a null body
     *     serialises as an empty 200 rather than a 404. It also throws
     *     IncorrectResultSizeDataAccessException the moment a customer has two
     *     accounts. Optional makes the empty case explicit; List would make the
     *     multiple case legal.
     * ----------------------------------------------------------------------
     *
     * Accounts findByCustomerId(long customerId);
     */
}
