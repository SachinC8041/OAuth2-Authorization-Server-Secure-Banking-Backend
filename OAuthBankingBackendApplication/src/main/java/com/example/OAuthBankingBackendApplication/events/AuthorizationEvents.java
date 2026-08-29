package com.example.OAuthBankingBackendApplication.events;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Audit trail for authorization denials.
 */
@Component
@Slf4j
public class AuthorizationEvents {

    /**
     * The previous version supplied three arguments for two placeholders and
     * passed the username twice, so the decision that caused the denial - the one
     * piece of information worth logging - was silently dropped.
     */
    @EventListener
    public void onFailure(AuthorizationDeniedEvent<?> deniedEvent) {
        log.warn("Authorization denied for {}: {}",
                usernameOf(deniedEvent.getAuthentication()),
                deniedEvent.getAuthorizationResult());
    }

    /**
     * The supplier is resolved defensively: it can yield {@code null} when a
     * request is denied before any principal has been established.
     */
    private String usernameOf(Supplier<Authentication> authenticationSupplier) {
        if (authenticationSupplier == null) {
            return "anonymous";
        }

        Authentication authentication = authenticationSupplier.get();
        return authentication == null ? "anonymous" : authentication.getName();
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original denial listener.
     *
     *     Three arguments for two placeholders, with the username passed twice.
     *     SLF4J fills placeholders left to right and silently discards the
     *     surplus, so getAuthorizationResult() - the only part explaining WHY
     *     the request was denied - never reached the log.
     * ----------------------------------------------------------------------
     *
     * @EventListener
     * public void onFailure(AuthorizationDeniedEvent event) {
     *     log.error("Authorization failed for the user {} due to {} ",
     *             event.getAuthentication().get().getName(),
     *             event.getAuthentication().get().getName(), event.getAuthorizationResult());
     * }
     */
}
