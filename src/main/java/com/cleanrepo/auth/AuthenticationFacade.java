package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.auth.exception.AttemptOfAuthenticateNotAuthenticatedEndpoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public int getAccountId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            var accountCredentials = (AccountCredentials) authentication.getPrincipal();
            return accountCredentials.getAccountId();
        } catch (ClassCastException e){
            throw new AttemptOfAuthenticateNotAuthenticatedEndpoint();
        }
    }

    public String getAccountEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            var accountCredentials = (AccountCredentials) authentication.getPrincipal();
            return accountCredentials.getEmail().getEmail();
        } catch (ClassCastException e){
            throw new AttemptOfAuthenticateNotAuthenticatedEndpoint();
        }
    }
}
