package com.example.OAuthBankingBackendApplication.filter;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import com.example.OAuthBankingBackendApplication.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the {@code Authorization} header, verifies the token and populates the
 * security context for the rest of the request.
 *
 * <p>A request without the header passes straight through. Whether it then
 * succeeds is decided by the authorization rules, not by this filter.
 *
 * <p>An invalid token is answered here, by delegating to the
 * {@link AuthenticationEntryPoint}, rather than by throwing. This filter sits
 * ahead of {@code BasicAuthenticationFilter} and therefore ahead of
 * {@code ExceptionTranslationFilter}, so an exception thrown from here would
 * escape the security chain entirely and surface as a container error page
 * instead of the intended {@code 401}.
 *
 * <p>Renamed from {@code JWTTokenValidationFilter}.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);

        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = jwtService.parseToken(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception exception) {
            // Logged at debug: a rejected token is a client problem, not a server fault,
            // and logging it at error level lets anyone fill the log by sending junk.
            log.debug("Rejected JWT on {}: {}", request.getRequestURI(), exception.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request, response, new BadCredentialsException("Invalid or expired JWT token"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Skips {@code /user}, which authenticates with HTTP Basic and is where
     * {@link JwtTokenGeneratorFilter} issues the token in the first place.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/user".equals(request.getServletPath());
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original body, which threw instead of writing a response.
     *
     *     This is the most instructive bug in the project. Throwing an
     *     AuthenticationException from a filter only produces a clean 401 if
     *     ExceptionTranslationFilter is DOWNSTREAM of the thrower - it catches
     *     exceptions coming back up from the filters it wraps. In the default
     *     chain the order is:
     *
     *       ... BasicAuthenticationFilter ... ExceptionTranslationFilter, AuthorizationFilter
     *
     *     and this filter is registered with addFilterBefore(...,
     *     BasicAuthenticationFilter.class), which puts it ahead of both. The
     *     exception therefore escapes the security chain entirely and reaches
     *     the servlet container, which renders a 500 error page. An invalid
     *     token looked like a server fault.
     *
     *     Calling the AuthenticationEntryPoint directly and returning without
     *     continuing the chain is the standard fix.
     *
     * Imports:
     *   io.jsonwebtoken.Claims
     *   io.jsonwebtoken.Jwts
     *   io.jsonwebtoken.security.Keys
     *   javax.crypto.SecretKey
     *   java.nio.charset.StandardCharsets
     *   org.springframework.core.env.Environment
     *   org.springframework.security.authentication.UsernamePasswordAuthenticationToken
     *   org.springframework.security.core.authority.AuthorityUtils
     * ----------------------------------------------------------------------
     *
     * String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);
     * if (jwt != null) {
     *     try {
     *         Environment env = getEnvironment();
     *         if (env != null) {
     *             String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
     *                     ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
     *             SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
     *
     *             if (null != secretKey) {
     *                 Claims claims = Jwts.parser()
     *                         .verifyWith(secretKey)
     *                         .build()
     *                         .parseSignedClaims(jwt)
     *                         .getPayload();
     *
     *                 String username = String.valueOf(claims.get("username"));
     *                 String authorities = String.valueOf(claims.get("authorities"));
     *
     *                 Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
     *                         AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
     *                 SecurityContextHolder.getContext().setAuthentication(authentication);
     *             }
     *         }
     *     } catch (Exception e) {
     *         throw new BadCredentialsException("Invalid JWT token received");
     *     }
     * }
     * filterChain.doFilter(request, response);
     */
}
