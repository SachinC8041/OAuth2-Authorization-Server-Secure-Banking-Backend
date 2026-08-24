package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.entity.LoginRequestDTO;
import com.example.OAuthBankingBackendApplication.entity.LoginResponseDTO;
import com.example.OAuthBankingBackendApplication.repository.CustomerRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final Environment env;
    private final AuthenticationManager authenticationManager;
    private static final long TOKEN_VALIDITY_MS = 30_000_000L;



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

    @PostMapping("/apiLogin")
    public ResponseEntity<LoginResponseDTO> apiLogin (@RequestBody LoginRequestDTO loginRequest) {

        String jwt = "";
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(),
                loginRequest.password());
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);
        if(null != authenticationResponse && authenticationResponse.isAuthenticated()) {
            if (null != env) {

                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                /*String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY);*/

                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                String authorities = authenticationResponse.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                jwt = Jwts.builder()
                        .issuer("SBI Bank")
                        .subject(authentication.getName())   // meaningful subject
                        .claim("username", authenticationResponse.getName())
                        .claim("authorities", authorities)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS))
                        .signWith(secretKey)
                        .compact();
            }
        }
        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER,jwt)
                .body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt));
    }
}
