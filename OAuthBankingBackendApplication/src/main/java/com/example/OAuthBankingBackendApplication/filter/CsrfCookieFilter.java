package com.example.OAuthBankingBackendApplication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forces the deferred CSRF token to be resolved so that Spring Security writes
 * the XSRF-TOKEN cookie on the response. Registered after
 * BasicAuthenticationFilter in both security configurations.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        // Render the token value to a cookie by causing the deferred token to be loaded.
        // NOTE: this NPEs if the attribute is absent (CSRF disabled, or the filter
        // ordered before CsrfFilter). A null guard would make it safe to reuse.
        csrfToken.getToken();

        filterChain.doFilter(request, response);
    }
}
