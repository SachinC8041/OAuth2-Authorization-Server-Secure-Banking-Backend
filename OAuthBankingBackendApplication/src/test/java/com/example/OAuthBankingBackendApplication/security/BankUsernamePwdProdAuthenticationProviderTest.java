package com.example.OAuthBankingBackendApplication.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * The production provider must behave exactly like the development one on the
 * basics. The pair exists to demonstrate profile selection, not to let the two
 * environments disagree about what a valid password is.
 */
@ExtendWith(MockitoExtension.class)
class BankUsernamePwdProdAuthenticationProviderTest {

    private static final String PASSWORD = "Password@12345";

    @Mock
    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;
    private BankUsernamePwdProdAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        provider = new BankUsernamePwdProdAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    private UserDetails storedUser() {
        return User.withUsername("sachin@example.com")
                .password(passwordEncoder.encode(PASSWORD))
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("the correct password authenticates")
    void correctPasswordAuthenticates() {
        when(userDetailsService.loadUserByUsername("sachin@example.com")).thenReturn(storedUser());

        Authentication result = provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", PASSWORD));

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getCredentials()).isNull();
    }

    @Test
    @DisplayName("a wrong password is rejected")
    void wrongPasswordIsRejected() {
        when(userDetailsService.loadUserByUsername("sachin@example.com")).thenReturn(storedUser());

        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", "nope")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("a missing credential is rejected before the user is loaded")
    void nullCredentialsAreRejected() {
        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sachin@example.com", null)))
                .isInstanceOf(BadCredentialsException.class);
    }
}
