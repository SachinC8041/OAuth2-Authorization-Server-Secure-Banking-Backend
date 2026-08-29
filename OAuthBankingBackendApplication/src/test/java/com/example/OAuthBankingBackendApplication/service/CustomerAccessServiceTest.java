package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Guards the fix for the insecure-direct-object-reference on the data endpoints.
 * If any of these stop passing, one customer can read another's records.
 */
@ExtendWith(MockitoExtension.class)
class CustomerAccessServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerAccessService customerAccessService;

    private static Authentication authenticationFor(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    private static Customer customerWithId(long id) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setEmail("sachin@example.com");
        return customer;
    }

    @Test
    @DisplayName("a caller may read their own customer id")
    void ownIdIsAllowed() {
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customerWithId(1L)));

        assertThatCode(() -> customerAccessService.requireOwnership(authenticationFor("sachin@example.com"), 1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a caller may not read somebody else's customer id")
    void anotherCustomersIdIsDenied() {
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customerWithId(1L)));

        assertThatThrownBy(() -> customerAccessService.requireOwnership(authenticationFor("sachin@example.com"), 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("the denial message does not reveal whether the requested id exists")
    void denialMessageDoesNotLeak() {
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customerWithId(1L)));

        assertThatThrownBy(() -> customerAccessService.requireOwnership(authenticationFor("sachin@example.com"), 999L))
                .hasMessageNotContaining("999");
    }

    @Test
    @DisplayName("a null authentication is denied")
    void nullAuthenticationIsDenied() {
        assertThatThrownBy(() -> customerAccessService.requireOwnership(null, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("a login with no matching customer row is denied")
    void unknownCustomerIsDenied() {
        when(customerRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                customerAccessService.requireAuthenticatedCustomer(authenticationFor("ghost@example.com")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("requireAuthenticatedCustomer returns the resolved customer")
    void resolvesAuthenticatedCustomer() {
        when(customerRepository.findByEmail("sachin@example.com"))
                .thenReturn(Optional.of(customerWithId(7L)));

        Customer resolved =
                customerAccessService.requireAuthenticatedCustomer(authenticationFor("sachin@example.com"));

        assertThat(resolved.getId()).isEqualTo(7L);
    }
}
