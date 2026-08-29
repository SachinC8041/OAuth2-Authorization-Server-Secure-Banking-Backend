package com.example.OAuthBankingBackendApplication.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * The single most important test class in the project: it pins down that the
 * provider actually checks the password. An earlier revision shipped a
 * non-production provider that authenticated any known username without one.
 */
@ExtendWith(MockitoExtension.class)
class BankUsernamePwdAuthenticationProviderTest {

    private static final String PASSWORD = "Password@12345";

    @Mock
    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;
    private BankUsernamePwdAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        provider = new BankUsernamePwdAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    private UserDetails storedUser() {
        return User.withUsername("sachin@example.com")
                .password(passwordEncoder.encode(PASSWORD))
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("the correct password authenticates and carries the stored authorities")
    void correctPasswordAuthenticates() {
        when(userDetailsService.loadUserByUsername("sachin@example.com")).thenReturn(storedUser());

        Authentication result = provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", PASSWORD));

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("a wrong password is rejected")
    void wrongPasswordIsRejected() {
        when(userDetailsService.loadUserByUsername("sachin@example.com")).thenReturn(storedUser());

        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", "not-the-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("an empty password is rejected")
    void emptyPasswordIsRejected() {
        when(userDetailsService.loadUserByUsername("sachin@example.com")).thenReturn(storedUser());

        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", "")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("a missing credential is rejected before the user is even loaded")
    void nullCredentialsAreRejected() {
        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", null)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("an unknown user propagates UsernameNotFoundException")
    void unknownUserPropagates() {
        when(userDetailsService.loadUserByUsername("ghost@example.com"))
                .thenThrow(new UsernameNotFoundException("no such customer"));

        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("ghost@example.com", PASSWORD)))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("the raw password is not carried into the authenticated token")
    void credentialsAreNotRetained() {
        when(userDetailsService.loadUserByUsername("sachin@example.com")).thenReturn(storedUser());

        Authentication result = provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", PASSWORD));

        assertThat(result.getCredentials()).isNull();
    }

    @Test
    @DisplayName("the provider claims only username-and-password tokens")
    void supportsOnlyUsernamePasswordTokens() {
        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
        assertThat(provider.supports(TestingAuthenticationToken.class)).isFalse();
    }
}
