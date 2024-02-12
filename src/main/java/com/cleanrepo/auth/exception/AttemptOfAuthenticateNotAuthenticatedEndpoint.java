package com.cleanrepo.auth.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttemptOfAuthenticateNotAuthenticatedEndpoint extends GenericAuthenticationException{

    public AttemptOfAuthenticateNotAuthenticatedEndpoint() {
        super("Attempt of authenticate endpoint marked as not authenticated");
        log.warn("Attempt of authenticate endpoint marked as not authenticated");
    }
}
