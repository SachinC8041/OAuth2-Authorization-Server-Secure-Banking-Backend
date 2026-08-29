package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Authority;
import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BankUserDetailsService bankUserDetailsService;

    private static Authority authority(String name) {
        Authority authority = new Authority();
        authority.setName(name);
        return authority;
    }

    private static Customer customer(Set<Authority> authorities) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("sachin@example.com");
        customer.setPwd("{noop}Password@12345");
        customer.setAuthorities(authorities);
        return customer;
    }

    @Test
    @DisplayName("a known e-mail resolves to UserDetails carrying the stored authorities")
    void loadsCustomerByEmail() {
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customer(new LinkedHashSet<>(Set.of(authority("ROLE_USER"))))));

        UserDetails userDetails = bankUserDetailsService.loadUserByUsername("sachin@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("sachin@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("{noop}Password@12345");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("an unknown e-mail raises UsernameNotFoundException")
    void unknownEmailThrows() {
        when(customerRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankUserDetailsService.loadUserByUsername("ghost@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost@example.com");
    }

    @Test
    @DisplayName("a blank authority row is skipped instead of breaking the login")
    void blankAuthorityIsSkipped() {
        Set<Authority> authorities = new LinkedHashSet<>();
        authorities.add(authority("ROLE_USER"));
        authorities.add(authority("  "));
        authorities.add(authority(null));
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customer(authorities)));

        UserDetails userDetails = bankUserDetailsService.loadUserByUsername("sachin@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("a customer with no authority rows still loads")
    void nullAuthoritySetIsTolerated() {
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customer(null)));

        assertThat(bankUserDetailsService.loadUserByUsername("sachin@example.com").getAuthorities()).isEmpty();
    }
}
