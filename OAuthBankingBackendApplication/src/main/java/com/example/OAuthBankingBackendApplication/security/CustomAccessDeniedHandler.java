package com.example.OAuthBankingBackendApplication.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Returns a JSON {@code 403} body when an authenticated caller lacks the
 * authority required for a request, or asks for data they do not own.
 *
 * <p>The diagnostic header uses the same {@code bank-error-} prefix as
 * {@link CustomAuthenticationEntryPoint} so that a client can look for one prefix
 * rather than two.
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    static final String DENIED_HEADER = "bank-error-reason";
    private static final String HEADER_VALUE = "Authorization failed";
    private static final String DEFAULT_MESSAGE = "Access denied";

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null)
                ? accessDeniedException.getMessage()
                : DEFAULT_MESSAGE;

        JsonErrorResponseWriter.write(
                request, response, HttpStatus.FORBIDDEN, DENIED_HEADER, HEADER_VALUE, message);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original body, building JSON with String.format.
     *
     *     Two issues. The values are not escaped, so an exception message
     *     containing a double quote or a backslash produces malformed JSON that
     *     the client cannot parse - and exception messages are exactly the place
     *     user-supplied text ends up. And the header was set to the constant
     *     DEFAULT_MESSAGE rather than the actual reason, so it said the same
     *     thing on every denial.
     *
     *     The header name also differed from the entry point's
     *     ("sbibank-denied-reason" here versus "bank-error-message" there),
     *     which meant a client had to look for two names to catch both cases.
     *
     * Imports:
     *   java.time.LocalDateTime
     * ----------------------------------------------------------------------
     *
     * private static final String DENIED_HEADER = "sbibank-denied-reason";
     *
     * LocalDateTime currentTimeStamp = LocalDateTime.now();
     * String message = (accessDeniedException != null && accessDeniedException.getMessage() != null)
     *         ? accessDeniedException.getMessage()
     *         : DEFAULT_MESSAGE;
     * String path = request.getRequestURI();
     *
     * response.setHeader(DENIED_HEADER, DEFAULT_MESSAGE);
     * response.setStatus(HttpStatus.FORBIDDEN.value());
     * response.setContentType("application/json;charset=UTF-8");
     *
     * String jsonResponse = String.format(
     *         "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
     *         currentTimeStamp, HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), message, path);
     *
     * response.getWriter().write(jsonResponse);
     */
}
