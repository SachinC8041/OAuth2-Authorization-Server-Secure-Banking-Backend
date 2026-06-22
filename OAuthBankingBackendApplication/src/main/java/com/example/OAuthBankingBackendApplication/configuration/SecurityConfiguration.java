package com.example.OAuthBankingBackendApplication.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration
{

    @Bean
    SecurityFilterChain customSecurityFilterChain(HttpSecurity http)
    {
        http.authorizeHttpRequests(request -> request
                .requestMatchers("/mycontact","/mynotices").permitAll()
                .requestMatchers("/myloans","/mybalance","/mycards").authenticated());
//        http.authorizeHttpRequests((request )-> request.anyRequest().permitAll());
//        http.authorizeHttpRequests((request )-> request.anyRequest().denyAll());
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        return http.build();
    }
}
