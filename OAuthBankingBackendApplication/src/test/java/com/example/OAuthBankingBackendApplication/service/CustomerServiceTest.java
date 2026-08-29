package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAccessService customerAccessService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private CustomerService service() {
        return new CustomerService(customerRepository, passwordEncoder, customerAccessService);
    }

    @Test
    @DisplayName("the submitted password is hashed before the row is saved")
    void hashesPasswordOnRegistration() {
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer submitted = new Customer();
        submitted.setEmail("sachin@example.com");
        submitted.setPwd("Password@12345");

        service().register(submitted);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());

        Customer saved = captor.getValue();
        assertThat(saved.getPwd()).isNotEqualTo("Password@12345");
        assertThat(passwordEncoder.matches("Password@12345", saved.getPwd())).isTrue();
        assertThat(saved.getCreateDt()).isNotNull();
    }
}
