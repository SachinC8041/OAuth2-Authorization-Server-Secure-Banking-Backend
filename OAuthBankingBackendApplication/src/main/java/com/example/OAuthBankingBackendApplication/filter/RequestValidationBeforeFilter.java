package com.example.OAuthBankingBackendApplication.filter;

import com.example.OAuthBankingBackendApplication.security.JsonErrorResponseWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Rejects HTTP Basic credentials before they reach the authentication providers.
 *
 * <p>Two rules are enforced:
 * <ul>
 *   <li>the Base64 payload must decode and contain a {@code username:password} separator;</li>
 *   <li>the username must not contain {@code test}, which is how the demo data set
 *       marks accounts that are never allowed to log in.</li>
 * </ul>
 *
 * <p>Like {@link JwtTokenValidatorFilter}, this filter writes its own response
 * rather than throwing. It runs ahead of {@code ExceptionTranslationFilter}, so a
 * thrown exception would leave the security chain and become a {@code 500}.
 */
public class RequestValidationBeforeFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private static final String BANNED_USERNAME_FRAGMENT = "test";
    private static final String ERROR_HEADER = "bank-error-reason";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(header) || !StringUtils.startsWithIgnoreCase(header.trim(), BASIC_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String encodedCredentials = header.trim().substring(BASIC_PREFIX.length());
        String decodedCredentials;
        try {
            decodedCredentials = new String(
                    Base64.getDecoder().decode(encodedCredentials.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            reject(request, response, "Failed to decode the basic authentication token");
            return;
        }

        int separatorIndex = decodedCredentials.indexOf(':');
        if (separatorIndex == -1) {
            reject(request, response, "Invalid basic authentication token");
            return;
        }

        String username = decodedCredentials.substring(0, separatorIndex);
        if (username.toLowerCase().contains(BANNED_USERNAME_FRAGMENT)) {
            reject(request, response, "This account is not permitted to authenticate");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        JsonErrorResponseWriter.write(
                request, response, HttpStatus.BAD_REQUEST, ERROR_HEADER, "Request validation failed", message);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original, implementing Filter directly.
     *
     *     Note the inconsistency: the banned-username case sets a status and
     *     returns, but the two malformed-token cases throw. Both throws land
     *     ahead of ExceptionTranslationFilter - see archived block [1] in
     *     JwtTokenValidatorFilter for why that matters - so a header this filter
     *     could not decode produced a 500 while a header it disliked produced a
     *     clean 400.
     *
     * Imports:
     *   jakarta.servlet.*
     *   org.springframework.security.authentication.BadCredentialsException
     * ----------------------------------------------------------------------
     *
     * public class RequestValidationBeforeFilter implements Filter {
     *
     *     @Override
     *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *             throws IOException, ServletException {
     *         HttpServletRequest req = (HttpServletRequest) request;
     *         HttpServletResponse res = (HttpServletResponse) response;
     *         String header = req.getHeader(HttpHeaders.AUTHORIZATION);
     *         if (null != header) {
     *             header = header.trim();
     *             if (StringUtils.startsWithIgnoreCase(header, "Basic ")) {
     *                 byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
     *                 byte[] decoded;
     *                 try {
     *                     decoded = Base64.getDecoder().decode(base64Token);
     *                     String token = new String(decoded, StandardCharsets.UTF_8); // un:pwd
     *                     int delim = token.indexOf(":");
     *                     if (delim == -1) {
     *                         throw new BadCredentialsException("Invalid basic authentication token");
     *                     }
     *                     String email = token.substring(0, delim);
     *                     if (email.toLowerCase().contains("test")) {
     *                         res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     *                         return;
     *                     }
     *                 } catch (IllegalArgumentException exception) {
     *                     throw new BadCredentialsException("Failed to decode basic authentication token");
     *                 }
     *             }
     *         }
     *         chain.doFilter(request, response);
     *     }
     * }
     */
}
