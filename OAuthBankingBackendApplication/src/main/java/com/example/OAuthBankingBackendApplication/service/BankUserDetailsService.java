package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads a {@link Customer} by email and adapts it to Spring Security's
 * {@link UserDetails} contract. Used by both authentication providers.
 */
@Service
@RequiredArgsConstructor
public class BankUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username :" + username));

        // NOTE: SimpleGrantedAuthority rejects a null or blank value, so a customer
        // row with an empty role column fails login with IllegalArgumentException
        // rather than a normal authentication error.
        // NOTE: roles are stored without the ROLE_ prefix, so hasRole("ADMIN") will
        // not match - use hasAuthority("ADMIN") if you add role-based rules later.
//        List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority(customer.getRole()));


        List<GrantedAuthority> authorities = customer.getAuthorities().stream().map(authority -> new
                SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList());
        return new User(customer.getEmail(), customer.getPwd(), authorities);
    }
}
