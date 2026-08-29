package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Authority;
import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Loads a {@link Customer} by e-mail address and adapts it to Spring Security's
 * {@link UserDetails} contract.
 *
 * <p>The login name for this application is the customer's e-mail, so
 * {@code authentication.getName()} returns an e-mail everywhere downstream.
 *
 * <p>Authorities come from the {@code authorities} table. The security rules use
 * {@code hasRole("USER")}, which Spring Security expands to the authority
 * {@code ROLE_USER}, so rows in that table must carry the {@code ROLE_} prefix.
 * See {@code docs/db/schema.sql} for the seed data this expects.
 */
@Service
@RequiredArgsConstructor
public class BankUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No customer found for e-mail: " + username));

        return new User(customer.getEmail(), customer.getPwd(), toAuthorities(customer.getAuthorities()));
    }

    /**
     * Converts the customer's authority rows into granted authorities.
     *
     * <p>Blank names are skipped on purpose. {@code SimpleGrantedAuthority} rejects
     * a null or empty value, so a single malformed row used to surface as an
     * {@code IllegalArgumentException} during login rather than as a normal
     * authentication failure.
     */
    private List<GrantedAuthority> toAuthorities(Set<Authority> authorities) {
        if (authorities == null) {
            return List.of();
        }

        return authorities.stream()
                .map(Authority::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> (GrantedAuthority) new SimpleGrantedAuthority(name))
                .toList();
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] Authorities taken from the single customer.role column instead of the
     *     authorities table. Replaces the toAuthorities(...) call.
     *
     *     Two traps this version has. SimpleGrantedAuthority rejects a null or
     *     blank value, so one customer row with an empty role column fails login
     *     with IllegalArgumentException rather than a normal authentication
     *     error. And role is stored without the ROLE_ prefix, so hasRole("ADMIN")
     *     never matches it - you would need hasAuthority("admin") instead.
     *
     *     One row per customer also means one authority per customer, which is
     *     why the project moved to the authorities table: it is a one-to-many.
     * ----------------------------------------------------------------------
     *
     * List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority(customer.getRole()));
     * return new User(customer.getEmail(), customer.getPwd(), grantedAuthorities);
     */

    /* ----------------------------------------------------------------------
     * [2] The authorities-table version as originally written, before the blank
     *     and null filtering was added.
     * ----------------------------------------------------------------------
     *
     * List<GrantedAuthority> authorities = customer.getAuthorities().stream()
     *         .map(authority -> new SimpleGrantedAuthority(authority.getName()))
     *         .collect(Collectors.toList());
     * return new User(customer.getEmail(), customer.getPwd(), authorities);
     */
}
