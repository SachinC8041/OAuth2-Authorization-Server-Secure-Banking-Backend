package com.example.OAuthBankingBackendApplication.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans that turn a username and password into an authenticated principal.
 *
 * <p>This class carries no {@code @Profile} on purpose. Both of these beans are
 * required under every profile - {@code UserController} cannot be constructed
 * without them - so putting them in a profile-scoped configuration is how the
 * production profile previously ended up unable to start.
 *
 * <p>Rule of thumb: a profiled configuration holds only what genuinely differs
 * between environments. Everything else belongs somewhere unprofiled.
 */
@Configuration
public class AuthenticationConfig {

    /**
     * Delegating encoder. Stored hashes carry an algorithm prefix such as
     * {@code {bcrypt}}, so the algorithm can be upgraded later without
     * invalidating passwords that were hashed with the old one.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Wraps whichever {@link AuthenticationProvider} the active profile supplies.
     *
     * <p>{@code BankUsernamePwdAuthenticationProvider} and
     * {@code BankUsernamePwdProdAuthenticationProvider} carry opposite
     * {@code @Profile} expressions, so exactly one of them is a bean at a time
     * and this parameter is never ambiguous. Injecting the interface rather than
     * naming a concrete class is what makes the profile switch actually take
     * effect - the original version hard-coded the production provider here,
     * which meant the development one was unreachable dead code no matter which
     * profile was active.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        ProviderManager providerManager = new ProviderManager(authenticationProvider);

        // Erase the raw password from the authenticated token once the check has
        // passed. Nothing downstream reads it. The original set this to false,
        // which left the plain-text password sitting in the security context for
        // the rest of the request.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }
}
