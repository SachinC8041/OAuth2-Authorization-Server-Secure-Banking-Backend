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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for every profile except {@code prod}.
 *
 * <p>The counterpart is {@link SecurityProdConfiguration}. The pair is kept
 * deliberately - profile-scoped configuration classes are the point of the
 * exercise - but it carries a real trade-off: two files this similar drift
 * apart. In the original version the production copy had lost its
 * {@code PasswordEncoder} and {@code AuthenticationManager} beans and all four
 * custom filters, so the {@code prod} profile could not start and had no token
 * validation. Anything that must be true in both places now lives in
 * {@link AuthenticationConfig}, and a single-class alternative is archived at
 * the bottom of this file.
 *
 * <p>Difference from the production configuration: HTTPS redirection is disabled
 * here rather than enforced.
 *
 * <h2>Filter order</h2>
 * <pre>
 *   RequestValidationBeforeFilter   (before BasicAuthenticationFilter)
 *   JwtTokenValidatorFilter         (before BasicAuthenticationFilter)
 *   BasicAuthenticationFilter
 *   CsrfCookieFilter                (after  BasicAuthenticationFilter)
 *   AuthoritiesLoggingAfterFilter   (after  BasicAuthenticationFilter)
 *   JwtTokenGeneratorFilter         (after  BasicAuthenticationFilter)
 * </pre>
 */
@Configuration
@Profile("!prod")
public class SecurityConfiguration {

    private final List<String> allowedOrigins;

    public SecurityConfiguration(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {

        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint();
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();

        // --- CORS ------------------------------------------------------------
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // --- Session / security context ---------------------------------------
        // Token based: nothing is kept server side between requests.
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // --- Channel security --------------------------------------------------
        // Disabled here, enforced in SecurityProdConfiguration. This is the one
        // genuine difference between the two profiles.
        http.redirectToHttps(https -> https.disable());

        // --- Authorization rules -------------------------------------------------
        // hasRole("USER") matches the authority ROLE_USER, so rows in the
        // authorities table must carry the ROLE_ prefix.
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(ApplicationConstants.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/account", "/loans", "/cards").hasRole("USER")
                .requestMatchers("/balance").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/user").authenticated()
                .anyRequest().authenticated());

        // --- CSRF -----------------------------------------------------------------
        // The token goes into a readable cookie so the SPA can echo it back in the
        // X-XSRF-TOKEN header. Endpoints reached before a token exists are exempt.
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
        // Form login is deliberately absent: it cannot work against a STATELESS
        // chain, and this is a JSON API with no server-rendered login page.
        http.httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint));

        // --- Exception handling ----------------------------------------------------------
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    /**
     * CORS policy for the browser client.
     *
     * <p>Origins come from {@code app.cors.allowed-origins}. The original version
     * declared a {@code FRONTEND_ORIGIN} constant on {@code http://} and then
     * ignored it in favour of an inline {@code https://} literal, so the dev
     * client was blocked by configuration that looked correct at a glance.
     */
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
     * [1] Stateful sessions with concurrent session control.
     *     Replaces the sessionManagement(...) line in the filter chain.
     *
     *     Only meaningful if the chain is NOT stateless - maximumSessions has
     *     nothing to count when no session is ever created. Switch the policy to
     *     ALWAYS first, which is why both lines appear together here.
     *
     * Imports:
     *   org.springframework.security.config.http.SessionCreationPolicy
     * ----------------------------------------------------------------------
     *
     * http.securityContext(contextConfig -> contextConfig.requireExplicitSave(false))
     *     .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
     *
     * http.sessionManagement(hsm -> hsm.invalidSessionUrl("/invalidUrl")
     *         .maximumSessions(3)
     *         .maxSessionsPreventsLogin(true)
     *         .expiredUrl("/expiredUrl"));
     */

    /* ----------------------------------------------------------------------
     * [2] Authority-based rules instead of role-based ones.
     *     Replaces the authorizeHttpRequests(...) block above.
     *
     *     The difference worth remembering: hasRole("USER") looks for the
     *     authority ROLE_USER, hasAuthority("VIEWACCOUNT") looks for exactly
     *     that string. Whichever you pick, the authorities table has to agree -
     *     a mismatch authenticates the customer and then denies them every
     *     endpoint, which reads like a broken login.
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
     * [3] Form login, and plain HTTP Basic without the custom entry point.
     *     Replaces the http.httpBasic(...) line.
     *
     *     formLogin needs a session-backed chain; against STATELESS it does
     *     nothing useful. withDefaults() on httpBasic gives the stock
     *     WWW-Authenticate challenge instead of the JSON 401 body.
     *
     * Imports:
     *   static org.springframework.security.config.Customizer.withDefaults
     * ----------------------------------------------------------------------
     *
     * http.formLogin(withDefaults());
     * http.httpBasic(withDefaults());
     */

    /* ----------------------------------------------------------------------
     * [4] AuthoritiesLoggingAtFilter, registered AT the position of
     *     BasicAuthenticationFilter rather than before or after it.
     *
     *     addFilterAt does not replace the filter already there, and the order
     *     between the two is undefined - which is why this stayed unused.
     * ----------------------------------------------------------------------
     *
     * .addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class);
     */

    /* ----------------------------------------------------------------------
     * [5] In-memory users, for exercising the chain without a database.
     *
     *     Declaring this UserDetailsService bean replaces BankUserDetailsService,
     *     so logins stop touching the customer table entirely. Note the two
     *     password prefixes: {noop} stores the password as-is, {bcrypt} marks it
     *     as already hashed. That prefix is what DelegatingPasswordEncoder reads
     *     to decide which encoder should verify it.
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
     *     Expects the users and authorities tables Spring Security ships with,
     *     which are not the shape of this project's customer table. Worth
     *     comparing against BankUserDetailsService, which exists precisely
     *     because the schema here is custom.
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
     *     Once this bean exists, Spring Security consults it during
     *     authentication and throws CompromisedPasswordException for a breached
     *     password. It makes an outbound HTTP call per login, so it belongs in
     *     the production configuration rather than here.
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

    /* ----------------------------------------------------------------------
     * [8] The single-class alternative to this profile pair.
     *
     *     Drop @Profile from this class, delete SecurityProdConfiguration, take
     *     Environment in the constructor, and branch on the one line that really
     *     differs. Fewer files and no drift, at the cost of making the profile
     *     mechanism less visible.
     *
     * Imports:
     *   org.springframework.core.env.Environment
     *   org.springframework.security.web.util.matcher.AnyRequestMatcher
     * ----------------------------------------------------------------------
     *
     * if (environment.matchesProfiles("prod")) {
     *     http.redirectToHttps(https -> https.requestMatchers(AnyRequestMatcher.INSTANCE));
     * } else {
     *     http.redirectToHttps(https -> https.disable());
     * }
     */
}
