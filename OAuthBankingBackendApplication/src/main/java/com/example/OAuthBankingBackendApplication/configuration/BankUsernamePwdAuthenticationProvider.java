package com.example.OAuthBankingBackendApplication.configuration;

import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Authentication provider for all non-production profiles.
 *
 * WARNING: this implementation does not verify the password. Any request
 * carrying a username that exists in the database is authenticated. The
 * password-checking version is kept in the ARCHIVED section at the bottom
 * of this file, and is what {@code BankUsernamePwdProdAuthenticationProvider}
 * already does for prod.
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class BankUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;

    /** Currently unused - required by the archived password check below. */
    private final PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] Password verification.
     *     Replaces the single return statement in authenticate(...).
     *
     * Imports:
     *   org.springframework.security.authentication.BadCredentialsException
     * ----------------------------------------------------------------------
     *
     * if (passwordEncoder.matches(password, userDetails.getPassword())) {
     *     return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
     * } else {
     *     throw new BadCredentialsException("Invalid password");
     * }
     */
}
