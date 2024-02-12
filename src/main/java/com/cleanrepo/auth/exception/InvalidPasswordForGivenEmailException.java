package com.cleanrepo.auth.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidPasswordForGivenEmailException extends GenericAuthenticationException {

    public InvalidPasswordForGivenEmailException() {
        super("Invalid password for given e-mail");
        log.warn("Invalid password for given e-mail");
    }
}
