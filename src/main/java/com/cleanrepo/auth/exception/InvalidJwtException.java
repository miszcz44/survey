package com.cleanrepo.auth.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidJwtException extends GenericAuthenticationException{

    public InvalidJwtException() {
        super("Could not create or verify jwt XX");
        log.warn("Could not create or verify jwt XX");
    }
}
