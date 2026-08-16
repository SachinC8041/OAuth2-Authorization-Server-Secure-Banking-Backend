package com.example.OAuthBankingBackendApplication.events;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Logs Spring Security authentication outcomes.
 */
@Component
@Slf4j
public class AuthenticationEvent {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("Authentication success for user {}", successEvent.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
        // NOTE: the failure reason is dropped. Adding failureEvent.getException()
        // as a second argument would log why the attempt failed.
        log.error("Authentication failure for user {} ", failureEvent.getAuthentication().getName());
    }
}
