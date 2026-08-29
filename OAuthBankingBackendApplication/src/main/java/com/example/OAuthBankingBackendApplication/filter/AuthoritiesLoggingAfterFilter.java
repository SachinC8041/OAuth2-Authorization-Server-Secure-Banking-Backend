package com.example.OAuthBankingBackendApplication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs who was authenticated and what they were granted, once the authentication
 * filters have run.
 *
 * <p>Diagnostic only - it never changes the outcome of a request.
 *
 * <p>Renamed from {@code AuthoritiesLogginAfterFilter} and switched from
 * {@link jakarta.servlet.Filter} to {@link OncePerRequestFilter} so it does not
 * run twice on forwards and errors. Messages use SLF4J placeholders rather than
 * string concatenation, so nothing is built when the level is disabled.
 */
@Slf4j
public class AuthoritiesLoggingAfterFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.debug("User {} is authenticated with authorities {}",
                    authentication.getName(), authentication.getAuthorities());
        }

        filterChain.doFilter(request, response);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original, implementing Filter directly and concatenating strings.
     *
     *     Two things changed. A plain Filter runs again on every forward and on
     *     the error dispatch, so one request could log the same line three
     *     times; OncePerRequestFilter is the Spring base class that prevents
     *     that. And string concatenation builds the whole message even when the
     *     level is disabled - SLF4J placeholders defer that work.
     *
     * Imports:
     *   jakarta.servlet.*
     * ----------------------------------------------------------------------
     *
     * public class AuthoritiesLogginAfterFilter implements Filter {
     *
     *     @Override
     *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *             throws IOException, ServletException {
     *         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
     *         if (null != authentication) {
     *             log.info("User " + authentication.getName() + " is successfully authenticated and "
     *                     + "has the authorities " + authentication.getAuthorities().toString());
     *         }
     *         chain.doFilter(request, response);
     *     }
     * }
     */

    /* ----------------------------------------------------------------------
     * [2] AuthoritiesLoggingAtFilter - the never-registered third variant.
     *     Referenced by archived block [4] in SecurityConfiguration.
     *
     *     Logged BEFORE the authentication filters rather than after, so the
     *     security context was still empty and it never had anything to print.
     * ----------------------------------------------------------------------
     *
     * public class AuthoritiesLoggingAtFilter implements Filter {
     *
     *     @Override
     *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *             throws IOException, ServletException {
     *         log.info("Authentication Validation is in progress");
     *         chain.doFilter(request, response);
     *     }
     * }
     */
}
