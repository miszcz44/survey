package com.cleanrepo.account.dto;

import com.cleanrepo.enums.ResponseStatus;

public record SignUpResponse (ResponseStatus code,
        String message,
        String token) {
}
