package com.example.OAuthBankingBackendApplication.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Authentication provider for the {@code prod} profile.
 *
 * <p>The counterpart to {@link BankUsernamePwdAuthenticationProvider}. Both now
 * verify the password identically; the pair exists to demonstrate profile-based
 * bean selection, which is why they are kept separate rather than merged.
 *
 * <p>A realistic reason to keep two: production wants extra checks that would
 * make local development painful. Account lockout after repeated failures, a
 * compromised-password lookup, or a mandatory second factor would live here and
 * not in the development provider.
 */
@Component
@Profile("prod")
@RequiredArgsConstructor
public class BankUsernamePwdProdAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();

        if (credentials == null) {
            throw new BadCredentialsException("No credentials supplied");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(credentials.toString(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] A production-only extra check: reject passwords that appear in known
     *     breach corpora. Goes inside authenticate(...), after the matches()
     *     check passes. Needs the CompromisedPasswordChecker bean archived in
     *     SecurityProdConfiguration.
     *
     *     This is the kind of difference that justifies a separate production
     *     provider: you do not want an outbound API call on every local login.
     *
     * Imports:
     *   org.springframework.security.authentication.password.CompromisedPasswordChecker
     *   org.springframework.security.authentication.password.CompromisedPasswordException
     * ----------------------------------------------------------------------
     *
     * if (compromisedPasswordChecker.check(credentials.toString()).isCompromised()) {
     *     throw new CompromisedPasswordException("The provided password is compromised");
     * }
     */
}
