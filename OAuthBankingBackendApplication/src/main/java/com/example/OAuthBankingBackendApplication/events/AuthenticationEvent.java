package com.example.OAuthBankingBackendApplication.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationEvent
{
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent)
    {
        log.info("Authentication success for user {}", successEvent.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent)
    {
        log.error("Authentication failure for user {} ", failureEvent.getAuthentication().getName());
    }
}
