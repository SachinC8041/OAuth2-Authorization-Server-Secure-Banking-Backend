package com.example.OAuthBankingBackendApplication.events;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorizationEvent {

    @EventListener
    public void onFailure(AuthorizationDeniedEvent event)
    {
        log.error("Authorization failed for the user {} due to {} ",event.getAuthentication().get().getName(), event.getAuthentication().get().getName(),
                event.getAuthorizationResult());
    }
}
