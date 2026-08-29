package com.example.OAuthBankingBackendApplication.events;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Audit trail for authentication outcomes.
 */
@Component
@Slf4j
public class AuthenticationEvents {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("Authentication succeeded for {}", successEvent.getAuthentication().getName());
    }

    /**
     * The exception is passed as the final argument so SLF4J logs the stack trace.
     * The previous version dropped it, which left "authentication failure" entries
     * with no indication of whether the cause was a bad password, an unknown user
     * or a locked account.
     */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log.warn("Authentication failed for {}",
                failureEvent.getAuthentication().getName(),
                failureEvent.getException());
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original failure listener.
     *
     *     The exception was never passed to the logger, so every failure logged
     *     the same line regardless of cause - bad password, unknown user, locked
     *     account, all identical. SLF4J treats a final Throwable argument
     *     specially and prints the stack trace, which is why the fixed version
     *     passes failureEvent.getException() as the last argument even though
     *     there is only one placeholder in the message.
     * ----------------------------------------------------------------------
     *
     * @EventListener
     * public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
     *     log.error("Authentication failure for user {} ", failureEvent.getAuthentication().getName());
     * }
     */
}
