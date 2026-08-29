package com.example.OAuthBankingBackendApplication.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Returns a JSON {@code 401} body instead of the container's default empty
 * response when a request arrives without usable credentials.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String ERROR_HEADER = "bank-error-reason";
    private static final String HEADER_VALUE = "Authentication failed";
    private static final String DEFAULT_MESSAGE = "Unauthorized";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        String message = (authenticationException != null && authenticationException.getMessage() != null)
                ? authenticationException.getMessage()
                : DEFAULT_MESSAGE;

        JsonErrorResponseWriter.write(
                request, response, HttpStatus.UNAUTHORIZED, ERROR_HEADER, HEADER_VALUE, message);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original body. Same unescaped String.format as the access denied
     *     handler; the shared writer exists so the two cannot drift apart again.
     *
     * Imports:
     *   java.time.LocalDateTime
     * ----------------------------------------------------------------------
     *
     * private static final String ERROR_HEADER = "bank-error-message";
     *
     * LocalDateTime currentTimeStamp = LocalDateTime.now();
     * String message = (authException != null && authException.getMessage() != null)
     *         ? authException.getMessage()
     *         : DEFAULT_MESSAGE;
     * String path = request.getRequestURI();
     *
     * response.setHeader(ERROR_HEADER, "Authentication failed");
     * response.setStatus(HttpStatus.UNAUTHORIZED.value());
     * response.setContentType("application/json;charset=UTF-8");
     *
     * String jsonResponse = String.format(
     *         "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
     *         currentTimeStamp, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), message, path);
     *
     * response.getWriter().write(jsonResponse);
     */
}
