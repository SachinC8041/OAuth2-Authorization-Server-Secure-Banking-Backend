package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Issues and verifies the application's JSON Web Tokens.
 *
 * <p>Token creation previously lived in three places - {@code UserController},
 * {@code JwtTokenGeneratorFilter} and {@code JwtTokenValidatorFilter} - each with
 * its own copy of the secret lookup, the claim names and the validity window.
 * Centralising it here means a change to the token format is a change to one file.
 *
 * <p>The signing key is resolved once at startup rather than on every request,
 * which also removes the previous reliance on
 * {@code OncePerRequestFilter#getEnvironment()}. Filters are not Spring beans, so
 * that call returned a standalone {@code Environment} that never saw
 * {@code application.properties} and therefore always fell through to the default
 * secret.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(Environment environment) {
        String secret = environment.getProperty(
                ApplicationConstants.JWT_SECRET_KEY,
                ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

        if (ApplicationConstants.JWT_SECRET_DEFAULT_VALUE.equals(secret)) {
            if (environment.matchesProfiles("prod")) {
                throw new IllegalStateException(
                        "The built-in development JWT secret cannot be used under the 'prod' profile. "
                                + "Set the " + ApplicationConstants.JWT_SECRET_KEY + " environment variable.");
            }
            log.warn("Using the built-in development JWT secret. Set {} before deploying anywhere real.",
                    ApplicationConstants.JWT_SECRET_KEY);
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds a signed token describing the given authenticated principal.
     *
     * @param authentication a successfully authenticated token
     * @return the compact serialised JWT
     */
    public String generateToken(Authentication authentication) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ApplicationConstants.JWT_VALIDITY);

        return Jwts.builder()
                .issuer(ApplicationConstants.JWT_ISSUER)
                .subject(authentication.getName())
                .claim(ApplicationConstants.CLAIM_USERNAME, authentication.getName())
                .claim(ApplicationConstants.CLAIM_AUTHORITIES, serialiseAuthorities(authentication))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies the signature and expiry of a token and rebuilds the principal it
     * describes.
     *
     * @param jwt the compact serialised JWT
     * @return an authenticated token ready to be placed in the security context
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired or
     *                                      signed with a different key
     */
    public Authentication parseToken(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String username = claims.get(ApplicationConstants.CLAIM_USERNAME, String.class);
        String authorities = claims.get(ApplicationConstants.CLAIM_AUTHORITIES, String.class);

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                AuthorityUtils.commaSeparatedStringToAuthorityList(
                        authorities == null ? "" : authorities));
    }

    private String serialiseAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
