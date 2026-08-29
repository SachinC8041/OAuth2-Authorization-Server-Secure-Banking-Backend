package com.example.OAuthBankingBackendApplication.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Turns credentials into a token.
 *
 * <p>Sits between the controller and the two pieces it needs -
 * {@link AuthenticationManager} to verify the password and {@link JwtService} to
 * mint the token - so the controller does nothing but translate the outcome into
 * an HTTP status.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * @return a signed JWT for the caller
     * @throws AuthenticationException if the credentials are wrong or the account
     *                                 cannot authenticate
     */
    public String login(String username, String password) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password));

        return jwtService.generateToken(authentication);
    }
}
