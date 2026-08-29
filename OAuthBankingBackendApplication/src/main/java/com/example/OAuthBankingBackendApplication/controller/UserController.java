package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import com.example.OAuthBankingBackendApplication.dto.LoginRequestDTO;
import com.example.OAuthBankingBackendApplication.dto.LoginResponseDTO;
import com.example.OAuthBankingBackendApplication.entity.Customer;
import com.example.OAuthBankingBackendApplication.service.AuthenticationService;
import com.example.OAuthBankingBackendApplication.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registration, login and current-user lookup.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final CustomerService customerService;
    private final AuthenticationService authenticationService;

    /**
     * Creates a customer. The submitted password is hashed before it is stored.
     *
     * @return {@code 201} on success, {@code 409} if the e-mail is already taken
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        try {
            customerService.register(customer);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Given user details are successfully registered");

        } catch (DataIntegrityViolationException exception) {
            // Caught specifically rather than swallowing every Exception. The
            // original catch(Exception) reported a mapping mistake or a dropped
            // connection to the caller as an ordinary registration failure.
            log.warn("Registration rejected for a duplicate or invalid customer record", exception);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A customer with those details already exists");
        }
    }

    /**
     * Exchanges a username and password for a signed JWT.
     *
     * <p>The token is returned twice on purpose: in the {@code Authorization}
     * response header, which the browser client reads, and in the body, which is
     * easier to work with from cURL and Postman.
     *
     * @return {@code 200} with the token, {@code 401} if the credentials are wrong
     */
    @PostMapping("/apiLogin")
    public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequest) {
        try {
            String jwt = authenticationService.login(loginRequest.username(), loginRequest.password());

            return ResponseEntity.ok()
                    .header(ApplicationConstants.JWT_HEADER, jwt)
                    .body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt));

        } catch (AuthenticationException exception) {
            // The original returned 200 with an empty token string on failure, so
            // a client could not tell success from failure by status code alone.
            log.debug("Login failed for {}: {}", loginRequest.username(), exception.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDTO(HttpStatus.UNAUTHORIZED.getReasonPhrase(), null));
        }
    }

    /**
     * Returns the customer behind the current authentication.
     *
     * <p>Changed from {@code @RequestMapping} to {@code @GetMapping}: the old
     * mapping answered every HTTP method, including {@code DELETE}, on a path
     * that only ever reads.
     */
    @GetMapping("/user")
    public ResponseEntity<Customer> getUserDetailsAfterLogin(Authentication authentication) {
        return ResponseEntity.ok(customerService.findAuthenticatedCustomer(authentication));
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The first registration controller (UserRegistrationController).
     *     Superseded by registerUser(...) above.
     *
     *     Two things worth remembering. The path was /registeruser while the
     *     security rules whitelisted /register, so the endpoint was permitted by
     *     one file and unreachable per the other. And HttpStatus.ACCEPTED (202)
     *     means "queued, not done yet" - the wrong code for a row that has
     *     already been written. CREATED (201) is the honest one.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.CustomerRepository
     *   org.springframework.security.crypto.password.PasswordEncoder
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

    /* ----------------------------------------------------------------------
     * [2] The second registration method, before the service layer existed.
     *
     *     catch(Exception) is the thing to look at. Every failure - a duplicate
     *     e-mail, a broken mapping, a dead database - became the same 500 with
     *     the same message, so the caller learned nothing and neither did you.
     * ----------------------------------------------------------------------
     *
     * @PostMapping("/register")
     * public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
     *     try {
     *         String hashPwd = passwordEncoder.encode(customer.getPwd());
     *         customer.setPwd(hashPwd);
     *         customer.setCreateDt(new Date(System.currentTimeMillis()));
     *
     *         Customer savedCustomer = customerRepository.save(customer);
     *
     *         if (savedCustomer.getId() != 0) {
     *             return ResponseEntity.status(HttpStatus.CREATED)
     *                     .body("Given user details are successfully registered");
     *         }
     *
     *         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User registration failed");
     *
     *     } catch (Exception ex) {
     *         log.error("User registration failed for email {}", customer.getEmail(), ex);
     *         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
     *     }
     * }
     */

    /* ----------------------------------------------------------------------
     * [3] The original apiLogin, which built the JWT inline.
     *
     *     This is the code that became JwtService. Three problems it shows:
     *
     *     - The same twenty lines existed here AND in JWTTokenGeneratorFiler,
     *       with the secret lookup, the claim names and the validity window
     *       duplicated. Change one and the other quietly disagrees.
     *     - The nested null checks mean a failed authentication falls through to
     *       the final return with jwt still "", so the caller gets 200 OK and an
     *       empty token instead of a 401.
     *     - .subject(authentication.getName()) reads the UNauthenticated token,
     *       not authenticationResponse. It happens to hold the same username, so
     *       the bug is invisible until something changes the principal during
     *       authentication.
     *
     *     The commented single-argument getProperty line is worth keeping in
     *     mind too: without the default it returns null, and Keys.hmacShaKeyFor
     *     then throws on startup rather than at first login.
     *
     * Imports:
     *   io.jsonwebtoken.Jwts
     *   io.jsonwebtoken.security.Keys
     *   javax.crypto.SecretKey
     *   java.nio.charset.StandardCharsets
     *   java.util.Date
     *   java.util.stream.Collectors
     *   org.springframework.core.env.Environment
     *   org.springframework.security.authentication.AuthenticationManager
     *   org.springframework.security.authentication.UsernamePasswordAuthenticationToken
     *   org.springframework.security.core.GrantedAuthority
     * ----------------------------------------------------------------------
     *
     * private static final long TOKEN_VALIDITY_MS = 30_000_000L;
     * private final Environment env;
     * private final AuthenticationManager authenticationManager;
     *
     * @PostMapping("/apiLogin")
     * public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequest) {
     *
     *     String jwt = "";
     *     Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(
     *             loginRequest.username(), loginRequest.password());
     *     Authentication authenticationResponse = authenticationManager.authenticate(authentication);
     *     if (null != authenticationResponse && authenticationResponse.isAuthenticated()) {
     *         if (null != env) {
     *
     *             String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
     *                     ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
     *             // String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY);
     *
     *             SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
     *
     *             String authorities = authenticationResponse.getAuthorities().stream()
     *                     .map(GrantedAuthority::getAuthority)
     *                     .collect(Collectors.joining(","));
     *
     *             jwt = Jwts.builder()
     *                     .issuer("SBI Bank")
     *                     .subject(authentication.getName())
     *                     .claim("username", authenticationResponse.getName())
     *                     .claim("authorities", authorities)
     *                     .issuedAt(new Date())
     *                     .expiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS))
     *                     .signWith(secretKey)
     *                     .compact();
     *         }
     *     }
     *     return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER, jwt)
     *             .body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt));
     * }
     */

    /* ----------------------------------------------------------------------
     * [4] The original /user handler.
     *
     *     @RequestMapping with no method attribute maps every verb, so DELETE
     *     /user and PUT /user both hit a read-only method. Returning
     *     optionalCustomer.orElse(null) also serialises as an empty 200 body
     *     rather than an error.
     * ----------------------------------------------------------------------
     *
     * @RequestMapping("/user")
     * public Customer getUserDetailsAfterLogin(Authentication authentication) {
     *     Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
     *     return optionalCustomer.orElse(null);
     * }
     */
}
