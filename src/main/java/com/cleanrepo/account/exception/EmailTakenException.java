package com.cleanrepo.account.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailTakenException extends GenericAccountException{

    public EmailTakenException() {
        super("This e-mail is already taken");
        log.warn("This e-mail is already taken");
    }

}
