package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtService jwtService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(ApplicationConstants.JWT_SECRET_KEY,
                "test-secret-that-is-long-enough-for-hs256-signing");
        jwtService = new JwtService(environment);
        authenticationService = new AuthenticationService(authenticationManager, jwtService);
    }

    @Test
    @DisplayName("valid credentials yield a token describing the authenticated user")
    void issuesTokenOnSuccess() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("sachin@example.com", null, authorities));

        String token = authenticationService.login("sachin@example.com", "Password@12345");

        Authentication parsed = jwtService.parseToken(token);
        assertThat(parsed.getName()).isEqualTo("sachin@example.com");
        assertThat(parsed.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("a rejected login propagates the AuthenticationException rather than returning an empty token")
    void propagatesFailure() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid password"));

        assertThatThrownBy(() -> authenticationService.login("sachin@example.com", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
