package com.example.OAuthBankingBackendApplication.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityHandlersTest {

    @Test
    @DisplayName("the entry point answers 401 with the failure reason")
    void entryPointWrites401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/cards");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CustomAuthenticationEntryPoint()
                .commence(request, response, new BadCredentialsException("Invalid or expired JWT token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("Invalid or expired JWT token");
    }

    @Test
    @DisplayName("the entry point falls back to a default message when there is none")
    void entryPointFallsBackToDefaultMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CustomAuthenticationEntryPoint()
                .commence(new MockHttpServletRequest("GET", "/cards"), response, null);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Unauthorized");
    }

    @Test
    @DisplayName("the access denied handler answers 403 with the denial reason")
    void accessDeniedHandlerWrites403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/loans");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CustomAccessDeniedHandler()
                .handle(request, response, new AccessDeniedException("The requested customer id is not available"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString())
                .contains("\"status\":403")
                .contains("not available");
    }

    @Test
    @DisplayName("both handlers use the same diagnostic header name")
    void handlersShareOneHeaderName() {
        assertThat(CustomAuthenticationEntryPoint.ERROR_HEADER)
                .isEqualTo(CustomAccessDeniedHandler.DENIED_HEADER);
    }
}
