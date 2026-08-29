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
 * Authentication provider for every profile except {@code prod}.
 *
 * <p>Paired with {@link BankUsernamePwdProdAuthenticationProvider}, which carries
 * the same {@code @Profile} annotation with the opposite expression. Exactly one
 * of the two is a bean at any time, which is what lets
 * {@code AuthenticationConfig} inject "the" {@link AuthenticationProvider}
 * without qualifying it.
 *
 * <p><b>Changed from the original.</b> This class used to skip the password check
 * entirely - any request naming a known user was authenticated. That version is
 * kept in the archived section at the bottom so the contrast is still visible,
 * but it is not what runs. A password-free provider is not a safe thing to have
 * on a branch that might get merged or demoed.
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class BankUsernamePwdAuthenticationProvider implements AuthenticationProvider {

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

        // Credentials are deliberately dropped from the authenticated token -
        // nothing downstream reads them, and leaving them out keeps the raw
        // password out of the security context.
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
     * [1] The original body of authenticate(...).
     *
     *     WARNING: this authenticates anyone whose username exists in the
     *     database, with no password check at all. It is here as a record of
     *     the learning-phase code, not as an option to switch back on.
     *
     *     What it teaches: an AuthenticationProvider decides for itself what
     *     "authenticated" means. Spring Security does not check the password
     *     for you - returning an authenticated token is the whole contract, so
     *     forgetting the check fails silently and open.
     * ----------------------------------------------------------------------
     *
     * String username = authentication.getName();
     * String password = authentication.getCredentials().toString();
     * UserDetails userDetails = userDetailsService.loadUserByUsername(username);
     *
     * return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
     */

    /* ----------------------------------------------------------------------
     * [2] The password check, as it was originally written - an if/else
     *     returning from the happy branch rather than a guard clause.
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
