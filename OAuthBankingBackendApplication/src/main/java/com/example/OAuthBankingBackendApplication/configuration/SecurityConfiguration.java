package com.example.OAuthBankingBackendApplication.configuration;

import com.example.OAuthBankingBackendApplication.filter.*;
import com.example.OAuthBankingBackendApplication.security.CustomAccessDeniedHandler;
import com.example.OAuthBankingBackendApplication.security.CustomAuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for all non-production profiles.
 *
 * Disabled / experimental configuration lives in the ARCHIVED CONFIGURATION
 * section at the bottom of this file.
 */
@Configuration
@Profile("!prod")
public class SecurityConfiguration {

    private static final String FRONTEND_ORIGIN = "http://localhost:4200";

    // ------------------------------------------------------------------
    // Beans
    // ------------------------------------------------------------------

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) {

        // --- CORS ----------------------------------------------------------
        http.cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Collections.singletonList("https://localhost:4200"));
                config.setAllowedMethods(Collections.singletonList("*"));
                config.setAllowedHeaders(Collections.singletonList("*"));
                config.setExposedHeaders(Arrays.asList("Authorization"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                return config;
            }
        }));

        // --- Session / security context -------------------------------------
        /*http.securityContext(contextConfig -> contextConfig.requireExplicitSave(false))
            .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));*/
        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // --- Channel security ------------------------------------------------
        http.redirectToHttps(https -> https.disable());

        // --- Authorization rules ---------------------------------------------
        /*http.authorizeHttpRequests(request -> request
                .requestMatchers("/account").hasAuthority("VIEWACCOUNT")
                .requestMatchers("/loans").hasAuthority("VIEWLOANS")
                .requestMatchers( "/balance").hasAuthority("VIEWBALANCE")
                .requestMatchers("/cards").hasAuthority("VIEWCARDS")
                .requestMatchers( "/user").authenticated()
                .requestMatchers("/notices", "/contact", "/error", "/register", "/invalidUrl", "/expiredUrl").permitAll()
                .anyRequest().authenticated());*/

        http.authorizeHttpRequests(request -> request
                .requestMatchers("/account").hasRole("USER")
                .requestMatchers("/loans").hasRole("USER")
                .requestMatchers( "/balance").hasAnyRole("USER","ADMIN")
                .requestMatchers("/cards").hasRole("USER")
                .requestMatchers( "/user").authenticated()
                .requestMatchers("/notices", "/contact", "/error", "/register", "/invalidUrl", "/expiredUrl","/apiLogin").permitAll()
                .anyRequest().authenticated());
        // --- CSRF -------------------------------------------------------------
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();

        http.csrf(csrfConfig -> csrfConfig
                        .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/contact", "/register","/apiLogin")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new AuthoritiesLogginAfterFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTTokenValidationFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTTokenGeneratorFiler(),BasicAuthenticationFilter.class);
        /*.addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class);*/

        // --- Authentication mechanisms ----------------------------------------
        http.formLogin(withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));

        // --- Exception handling ------------------------------------------------
        http.exceptionHandling(cad -> cad.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService , PasswordEncoder passwordEncoder)
    {
        BankUsernamePwdProdAuthenticationProvider authenticationProvider = new BankUsernamePwdProdAuthenticationProvider(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }

    /* ======================================================================
     * ARCHIVED CONFIGURATION - nothing below this line is active.
     * Uncomment a block to re-enable it and add the imports listed with it.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] Concurrent session control.
     *     Goes back INSIDE customSecurityFilterChain(), MERGED into the
     *     existing sessionManagement(...) call - do not declare it twice.
     * ----------------------------------------------------------------------
     *
     * http.sessionManagement(hsm -> hsm.invalidSessionUrl("/invalidUrl")
     *         .maximumSessions(3)
     *         .maxSessionsPreventsLogin(true)
     *         .expiredUrl("/expiredUrl"));
     */

    /* ----------------------------------------------------------------------
     * [2] Plain HTTP Basic, without the custom entry point.
     *     Replaces the http.httpBasic(...) line in the filter chain.
     * ----------------------------------------------------------------------
     *
     * http.httpBasic(withDefaults());
     */

    /* ----------------------------------------------------------------------
     * [3] In-memory users (quick local testing).
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
     * [4] JDBC-backed users (default Spring schema).
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
     * [5] Compromised-password check (HaveIBeenPwned API).
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
