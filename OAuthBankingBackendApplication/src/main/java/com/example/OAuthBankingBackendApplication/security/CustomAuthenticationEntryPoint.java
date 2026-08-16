package com.example.OAuthBankingBackendApplication.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns a JSON 401 body instead of the default empty response when a request
 * is not authenticated.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String ERROR_HEADER = "bank-error-message";
    private static final String DEFAULT_MESSAGE = "Unauthorized";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Populate dynamic values
        LocalDateTime currentTimeStamp = LocalDateTime.now();
        String message = (authException != null && authException.getMessage() != null)
                ? authException.getMessage()
                : DEFAULT_MESSAGE;
        String path = request.getRequestURI();

        response.setHeader(ERROR_HEADER, "Authentication failed");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        // Construct the JSON response
        // NOTE: values are not escaped. A message containing a double quote or a
        // backslash will produce malformed JSON - consider ObjectMapper instead.
        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                currentTimeStamp,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                path);

        response.getWriter().write(jsonResponse);
    }
}
