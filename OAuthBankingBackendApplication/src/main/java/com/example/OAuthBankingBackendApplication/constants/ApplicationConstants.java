package com.example.OAuthBankingBackendApplication.constants;

import java.time.Duration;

/**
 * Application-wide constants.
 *
 * <p>Every JWT-related value used by the application is declared here so that the
 * token issued by {@code POST /apiLogin} and the token accepted by the validation
 * filter can never drift apart.
 */
public final class ApplicationConstants {

    // ------------------------------------------------------------------
    // JWT
    // ------------------------------------------------------------------

    /**
     * Property (or OS environment variable) that holds the HMAC signing secret.
     * Must be at least 32 characters so that {@code HS256} has a 256-bit key.
     */
    public static final String JWT_SECRET_KEY = "JWT_SECRET";

    /**
     * Fallback secret used when {@link #JWT_SECRET_KEY} is not set.
     *
     * <p>This exists only so the application starts out of the box on a developer
     * machine. The application refuses to start under the {@code prod} profile if
     * this value is still in use - see
     * {@code com.example.OAuthBankingBackendApplication.service.JwtService}.
     */
    public static final String JWT_SECRET_DEFAULT_VALUE =
            "ABRAKADABRA!@#$%^&*abcdefghijklmnopqrstuvwxYZ";

    /** Request and response header that carries the bearer token. */
    public static final String JWT_HEADER = "Authorization";

    /** {@code iss} claim written into every issued token. */
    public static final String JWT_ISSUER = "SBI Bank";

    /** How long an issued token stays valid. */
    public static final Duration JWT_VALIDITY = Duration.ofHours(8);

    /** Custom claim holding the authenticated user's login name (their e-mail). */
    public static final String CLAIM_USERNAME = "username";

    /** Custom claim holding the comma-separated granted authorities. */
    public static final String CLAIM_AUTHORITIES = "authorities";

    // ------------------------------------------------------------------
    // Endpoints
    // ------------------------------------------------------------------

    /** Endpoints reachable without authentication. */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/notices", "/contact", "/error", "/register", "/apiLogin", "/invalidUrl", "/expiredUrl"
    };

    /** Endpoints exempt from CSRF because they are called before a session exists. */
    public static final String[] CSRF_EXEMPT_ENDPOINTS = {
            "/contact", "/register", "/apiLogin"
    };

    private ApplicationConstants() {
        throw new AssertionError("ApplicationConstants is a constant holder and must not be instantiated");
    }
}
