package com.example.OAuthBankingBackendApplication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forces the deferred CSRF token to be resolved so that Spring Security writes the
 * {@code XSRF-TOKEN} cookie onto the response.
 *
 * <p>Spring Security loads the token lazily; if nothing ever reads it, the cookie
 * is never written and the Angular client has nothing to echo back. Touching
 * {@link CsrfToken#getToken()} is what triggers the write.
 *
 * <p>The null check matters: when CSRF protection is disabled, or this filter is
 * ordered ahead of {@code CsrfFilter}, the request attribute is absent. The
 * previous version dereferenced it unconditionally and failed with a
 * {@code NullPointerException} on the very first request.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        filterChain.doFilter(request, response);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original body, without the null guard.
     *
     *     The attribute is absent whenever CSRF protection is disabled, or this
     *     filter is ordered ahead of CsrfFilter instead of after it. Either way
     *     the unguarded call throws NullPointerException on the first request,
     *     from inside a filter - so it surfaces as a container 500 with a stack
     *     trace that points at Spring rather than at this line.
     * ----------------------------------------------------------------------
     *
     * CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
     * csrfToken.getToken();
     * filterChain.doFilter(request, response);
     */
}
