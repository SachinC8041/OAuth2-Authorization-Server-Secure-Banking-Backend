package com.example.OAuthBankingBackendApplication.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns a JSON 403 body when an authenticated user lacks the required
 * authority for a request.
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // NOTE: CustomAuthenticationEntryPoint uses "bank-error-message". Worth
    // settling on one prefix for both handlers.
    private static final String DENIED_HEADER = "sbibank-denied-reason";
    private static final String DEFAULT_MESSAGE = "Authorization failed";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Populate dynamic values
        LocalDateTime currentTimeStamp = LocalDateTime.now();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null)
                ? accessDeniedException.getMessage()
                : DEFAULT_MESSAGE;
        String path = request.getRequestURI();

        response.setHeader(DENIED_HEADER, DEFAULT_MESSAGE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        // Construct the JSON response
        // NOTE: values are not escaped. A message containing a double quote or a
        // backslash will produce malformed JSON - consider ObjectMapper instead.
        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                currentTimeStamp,
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                message,
                path);

        response.getWriter().write(jsonResponse);
    }
}
