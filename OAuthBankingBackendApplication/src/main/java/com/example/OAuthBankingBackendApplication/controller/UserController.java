package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Optional;

/**
 * Registration and logged-in user lookup.
 *
 * An earlier version of this controller is kept in the ARCHIVED section at
 * the bottom of this file.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        try {
            String hashPwd = passwordEncoder.encode(customer.getPwd());
            customer.setPwd(hashPwd);
            customer.setCreateDt(new Date(System.currentTimeMillis()));

            Customer savedCustomer = customerRepository.save(customer);

            if (savedCustomer.getId() != 0) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Given user details are successfully registered");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User registration failed");

        } catch (Exception ex) {
            log.error("User registration failed for email {}", customer.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed");
        }
    }

    @RequestMapping("/user")
    public Customer getUserDetailsAfterLogin(Authentication authentication) {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] Previous registration controller (UserRegistrationController).
     *     Superseded by registerUser(...) above. Note the older /registeruser
     *     path and the HttpStatus.ACCEPTED response - SecurityProdConfiguration
     *     still whitelists /registeruser rather than /register.
     * ----------------------------------------------------------------------
     *
     * @RestController
     * @RequiredArgsConstructor
     * public class UserRegistrationController {
     *
     *     private final CustomerRepository customerRepository;
     *     private final PasswordEncoder passwordEncoder;
     *
     *     @PostMapping("/registeruser")
     *     public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
     *         try {
     *             String hashedPass = passwordEncoder.encode(customer.getPwd());
     *             customer.setPwd(hashedPass);
     *             Customer savedCustomer = customerRepository.save(customer);
     *             if (savedCustomer.getId() > 0) {
     *                 return ResponseEntity.status(HttpStatus.ACCEPTED).body("Customer registered successfully");
     *             } else {
     *                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Customer not registered successfully");
     *             }
     *         } catch (Exception e) {
     *             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception Occured" + e.getMessage());
     *         }
     *     }
     * }
     */
}
