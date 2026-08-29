package com.example.OAuthBankingBackendApplication.filter;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import com.example.OAuthBankingBackendApplication.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Hands a freshly signed JWT back to a caller who has just authenticated with
 * HTTP Basic against {@code /user}.
 *
 * <p>This supports the "log in with Basic, then use the token" flow. Clients that
 * post credentials to {@code /apiLogin} get their token from the response body
 * instead; both paths now build the token through the same {@link JwtService}.
 *
 * <p>Renamed from {@code JWTTokenGeneratorFiler}.
 */
@RequiredArgsConstructor
public class JwtTokenGeneratorFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            response.setHeader(ApplicationConstants.JWT_HEADER, jwtService.generateToken(authentication));
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Runs on {@code /user} only. Every other path either needs no token or gets
     * one from {@code /apiLogin}.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/user".equals(request.getServletPath());
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original body, building the token inline.
     *
     *     getEnvironment() is the interesting part. It comes from
     *     GenericFilterBean, and Spring only populates it when the filter is a
     *     bean. These filters are constructed with new(...) inside the security
     *     configuration, so Spring never calls setEnvironment and the getter
     *     lazily creates a StandardServletEnvironment instead. That environment
     *     reads system properties and servlet init parameters but NOT
     *     application.properties - so JWT_SECRET set in the properties file was
     *     never found here, and the default was used every time. It only looked
     *     like it worked because the two-argument getProperty always had a
     *     fallback.
     *
     *     Resolving the key once in a real bean (JwtService) fixes that and
     *     stops re-deriving the HMAC key on every request.
     *
     * Imports:
     *   io.jsonwebtoken.Jwts
     *   io.jsonwebtoken.security.Keys
     *   javax.crypto.SecretKey
     *   java.nio.charset.StandardCharsets
     *   java.util.Date
     *   java.util.stream.Collectors
     *   org.springframework.core.env.Environment
     *   org.springframework.security.core.GrantedAuthority
     * ----------------------------------------------------------------------
     *
     * private static final long TOKEN_VALIDITY_MS = 30_000_000L;
     *
     * Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
     * if (null != authentication) {
     *     Environment env = getEnvironment();
     *     if (null != env) {
     *         String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
     *                 ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
     *         // String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY);
     *
     *         SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
     *
     *         String authorities = authentication.getAuthorities().stream()
     *                 .map(GrantedAuthority::getAuthority)
     *                 .collect(Collectors.joining(","));
     *
     *         String jwt = Jwts.builder()
     *                 .issuer("SBI Bank")
     *                 .subject(authentication.getName())
     *                 .claim("username", authentication.getName())
     *                 .claim("authorities", authorities)
     *                 .issuedAt(new Date())
     *                 .expiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS))
     *                 .signWith(secretKey)
     *                 .compact();
     *
     *         response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
     *     }
     * }
     * filterChain.doFilter(request, response);
     */
}
