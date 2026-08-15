    package com.example.OAuthBankingBackendApplication.configuration;

    import com.example.OAuthBankingBackendApplication.security.CustomAccessDeniedHandler;
    import com.example.OAuthBankingBackendApplication.security.CustomAuthenticationEntryPoint;
    import jakarta.servlet.http.HttpServletRequest;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.annotation.Profile;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.crypto.factory.PasswordEncoderFactories;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.util.matcher.AnyRequestMatcher;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;

    import java.util.Collections;

    import static org.springframework.security.config.Customizer.withDefaults;

    @Configuration
    @Profile("prod")
    public class SecurityProdConfiguration {

        @Bean
        public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) {
            http.sessionManagement(hsm->hsm.invalidSessionUrl("/invalidsession")
                                                                                .maximumSessions(3)
                                                                                .maxSessionsPreventsLogin(true)
                                                                                .expiredUrl("/expiredUrl"));
            http.cors(corsConfig->corsConfig.configurationSource(new CorsConfigurationSource() {
                @Override
                public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }
            }));

            http.redirectToHttps((https) -> https.requestMatchers(AnyRequestMatcher.INSTANCE));
            http.csrf(csrfconfig->csrfconfig.disable());
            http.authorizeHttpRequests(request -> request
                    .requestMatchers("/myaccount", "/myloans", "/mybalance", "/mycards","/user").authenticated()
                    .requestMatchers("/mynotices", "/mycontact", "/error","/registeruser","/invalidSession","/expiredUrl").permitAll()
                    .anyRequest().authenticated());
            http.formLogin(withDefaults());
//            http.httpBasic(withDefaults());
            http.httpBasic(hbc-> hbc.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
            http.exceptionHandling(cad->cad.accessDeniedHandler(new CustomAccessDeniedHandler()));
            return http.build();
        }

        /*@Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.withUsername("sachin").password("{noop}P@$$word@1234").roles("USER").build();
            UserDetails admin = User.withUsername("suraj").password("{bcrypt}$2a$12$qV7DKGF.5Yv35LUT46FAy.4t2N2xfzfblEf/CXAaZO9LZUr5ZRiNa").roles("ADMIN").build();
            return new InMemoryUserDetailsManager(user, admin);
        }*/

        /*@Bean
        public UserDetailsService userDetailsService(DataSource dataSource)
        {
            return new JdbcUserDetailsManager(dataSource);
        }*/

        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        /*@Bean
        public CompromisedPasswordChecker checkCompromisedPassword()
        {
            return new HaveIBeenPwnedRestApiPasswordChecker();
        }*/
    }
