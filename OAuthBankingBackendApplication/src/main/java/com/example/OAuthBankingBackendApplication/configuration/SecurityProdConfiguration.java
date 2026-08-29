package com.example.OAuthBankingBackendApplication.configuration;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import com.example.OAuthBankingBackendApplication.filter.AuthoritiesLoggingAfterFilter;
import com.example.OAuthBankingBackendApplication.filter.CsrfCookieFilter;
import com.example.OAuthBankingBackendApplication.filter.JwtTokenGeneratorFilter;
import com.example.OAuthBankingBackendApplication.filter.JwtTokenValidatorFilter;
import com.example.OAuthBankingBackendApplication.filter.RequestValidationBeforeFilter;
import com.example.OAuthBankingBackendApplication.security.CustomAccessDeniedHandler;
import com.example.OAuthBankingBackendApplication.security.CustomAuthenticationEntryPoint;
import com.example.OAuthBankingBackendApplication.service.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the {@code prod} profile.
 *
 * <p>Difference from {@link SecurityConfiguration}: HTTPS is enforced for every
 * request instead of disabled. Everything else is intentionally identical, and
 * keeping it that way by hand is the cost of running two configuration classes.
 *
 * <p>Three things were missing from the original version of this file and are
 * restored here, because they are what made the {@code prod} profile unusable:
 * <ul>
 *   <li>The four custom filters were commented out, so production ran with no
 *       JWT validation at all - a token was never checked.</li>
 *   <li>The {@code PasswordEncoder} and {@code AuthenticationManager} beans were
 *       commented out. {@code UserController} requires both, so the context
 *       failed to start. Those two now live in {@link AuthenticationConfig},
 *       which carries no {@code @Profile} and so applies to both.</li>
 *   <li>Public endpoints referenced {@code /registeruser}, a path the controller
 *       had already stopped serving.</li>
 * </ul>
 */
@Configuration
@Profile("prod")
public class SecurityProdConfiguration {

    private final List<String> allowedOrigins;

    public SecurityProdConfiguration(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {

        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint();
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();

        // --- CORS ------------------------------------------------------------
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // --- Session / security context ---------------------------------------
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // --- Channel security --------------------------------------------------
        // The one genuine difference from the non-production chain: every request
        // is redirected to HTTPS. A bearer token sent over plain HTTP is readable
        // by anything on the path, so this is not optional in production.
        http.redirectToHttps(https -> https.requestMatchers(AnyRequestMatcher.INSTANCE));

        // --- Authorization rules -------------------------------------------------
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(ApplicationConstants.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/account", "/loans", "/cards").hasRole("USER")
                .requestMatchers("/balance").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/user").authenticated()
                .anyRequest().authenticated());

        // --- CSRF -----------------------------------------------------------------
        http.csrf(csrf -> csrf
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(ApplicationConstants.CSRF_EXEMPT_ENDPOINTS)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

        // --- Custom filters ---------------------------------------------------------
        http.addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JwtTokenValidatorFilter(jwtService, authenticationEntryPoint),
                        BasicAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JwtTokenGeneratorFilter(jwtService), BasicAuthenticationFilter.class);

        // --- Authentication mechanisms ------------------------------------------------
        http.httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint));

        // --- Exception handling ----------------------------------------------------------
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(ApplicationConstants.JWT_HEADER));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    /* ======================================================================
     * ARCHIVED CONFIGURATION - nothing below this line is active.
     * Uncomment a block to re-enable it and add the imports listed with it.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The authentication beans as they were originally declared here.
     *
     *     These now live in AuthenticationConfig, which has no @Profile. Leaving
     *     them in a profile-scoped class is exactly how the prod profile ended
     *     up unable to start: comment them out on one side of the pair and the
     *     application still compiles, still passes a context test under the
     *     default profile, and fails only when prod is actually selected.
     *
     *     The rule of thumb: put in a profiled configuration only what genuinely
     *     differs between environments. Beans that must always exist belong in
     *     an unprofiled one.
     *
     * Imports:
     *   org.springframework.security.authentication.AuthenticationManager
     *   org.springframework.security.authentication.ProviderManager
     *   org.springframework.security.core.userdetails.UserDetailsService
     *   org.springframework.security.crypto.factory.PasswordEncoderFactories
     *   org.springframework.security.crypto.password.PasswordEncoder
     *   com.example.OAuthBankingBackendApplication.security.BankUsernamePwdProdAuthenticationProvider
     * ----------------------------------------------------------------------
     *
     * @Bean
     * PasswordEncoder passwordEncoder() {
     *     return PasswordEncoderFactories.createDelegatingPasswordEncoder();
     * }
     *
     * @Bean
     * public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
     *                                                    PasswordEncoder passwordEncoder) {
     *     BankUsernamePwdProdAuthenticationProvider authenticationProvider =
     *             new BankUsernamePwdProdAuthenticationProvider(userDetailsService, passwordEncoder);
     *     ProviderManager providerManager = new ProviderManager(authenticationProvider);
     *     providerManager.setEraseCredentialsAfterAuthentication(false);
     *     return providerManager;
     * }
     */

    /* ----------------------------------------------------------------------
     * [2] Stateful sessions with concurrent session control.
     *     Replaces the sessionManagement(...) line in the filter chain.
     *
     * Imports:
     *   org.springframework.security.config.http.SessionCreationPolicy
     * ----------------------------------------------------------------------
     *
     * http.securityContext(contextConfig -> contextConfig.requireExplicitSave(false))
     *     .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
     *
     * http.sessionManagement(hsm -> hsm.invalidSessionUrl("/invalidsession")
     *         .maximumSessions(3)
     *         .maxSessionsPreventsLogin(true)
     *         .expiredUrl("/expiredUrl"));
     */

    /* ----------------------------------------------------------------------
     * [3] Authority-based rules instead of role-based ones.
     *     Replaces the authorizeHttpRequests(...) block above.
     * ----------------------------------------------------------------------
     *
     * http.authorizeHttpRequests(request -> request
     *         .requestMatchers("/account").hasAuthority("VIEWACCOUNT")
     *         .requestMatchers("/loans").hasAuthority("VIEWLOANS")
     *         .requestMatchers("/balance").hasAuthority("VIEWBALANCE")
     *         .requestMatchers("/cards").hasAuthority("VIEWCARDS")
     *         .requestMatchers("/user").authenticated()
     *         .requestMatchers("/notices", "/contact", "/error", "/register", "/invalidUrl", "/expiredUrl").permitAll()
     *         .anyRequest().authenticated());
     */

    /* ----------------------------------------------------------------------
     * [4] Form login, and plain HTTP Basic without the custom entry point.
     *     Replaces the http.httpBasic(...) line.
     *
     * Imports:
     *   static org.springframework.security.config.Customizer.withDefaults
     * ----------------------------------------------------------------------
     *
     * http.formLogin(withDefaults());
     * http.httpBasic(withDefaults());
     */

    /* ----------------------------------------------------------------------
     * [5] In-memory users. Kept only so this file stays parallel with
     *     SecurityConfiguration - hard-coded credentials have no business in a
     *     production profile, which is itself the lesson.
     *
     * Imports:
     *   org.springframework.security.core.userdetails.User
     *   org.springframework.security.core.userdetails.UserDetails
     *   org.springframework.security.core.userdetails.UserDetailsService
     *   org.springframework.security.provisioning.InMemoryUserDetailsManager
     * ----------------------------------------------------------------------
     *
     * @Bean
     * public UserDetailsService userDetailsService() {
     *     UserDetails user = User.withUsername("sachin")
     *             .password("{noop}P@$$word@1234")
     *             .roles("USER")
     *             .build();
     *     UserDetails admin = User.withUsername("suraj")
     *             .password("{bcrypt}$2a$12$qV7DKGF.5Yv35LUT46FAy.4t2N2xfzfblEf/CXAaZO9LZUr5ZRiNa")
     *             .roles("ADMIN")
     *             .build();
     *     return new InMemoryUserDetailsManager(user, admin);
     * }
     */

    /* ----------------------------------------------------------------------
     * [6] JDBC-backed users using Spring's default schema.
     *
     * Imports:
     *   javax.sql.DataSource
     *   org.springframework.security.core.userdetails.UserDetailsService
     *   org.springframework.security.provisioning.JdbcUserDetailsManager
     * ----------------------------------------------------------------------
     *
     * @Bean
     * public UserDetailsService userDetailsService(DataSource dataSource) {
     *     return new JdbcUserDetailsManager(dataSource);
     * }
     */

    /* ----------------------------------------------------------------------
     * [7] Compromised-password check against the HaveIBeenPwned API.
     *
     *     This is the block that genuinely belongs in the production profile
     *     rather than the development one: it costs an outbound HTTP call on
     *     every login. Pair it with archived block [1] in
     *     BankUsernamePwdProdAuthenticationProvider.
     *
     * Imports:
     *   org.springframework.security.authentication.password.CompromisedPasswordChecker
     *   org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker
     * ----------------------------------------------------------------------
     *
     * @Bean
     * public CompromisedPasswordChecker checkCompromisedPassword() {
     *     return new HaveIBeenPwnedRestApiPasswordChecker();
     * }
     */
}
