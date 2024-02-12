package com.cleanrepo.auth.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LockedAccountException extends GenericAuthenticationException {

    public LockedAccountException() {
        super("User account is locked");
        log.warn("User account is locked");
    }
}
