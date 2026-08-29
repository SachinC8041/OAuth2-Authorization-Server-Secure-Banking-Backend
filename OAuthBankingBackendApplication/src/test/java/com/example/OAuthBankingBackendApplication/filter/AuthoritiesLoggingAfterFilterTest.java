package com.example.OAuthBankingBackendApplication.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthoritiesLoggingAfterFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("the chain continues when a principal is present")
    void continuesWithAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "sachin@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MockFilterChain chain = new MockFilterChain();

        new AuthoritiesLoggingAfterFilter().doFilter(
                new MockHttpServletRequest("GET", "/account"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("the chain continues when there is no principal")
    void continuesWithoutAuthentication() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        new AuthoritiesLoggingAfterFilter().doFilter(
                new MockHttpServletRequest("GET", "/notices"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }
}
