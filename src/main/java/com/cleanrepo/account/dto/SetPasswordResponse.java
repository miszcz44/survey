package com.cleanrepo.account.dto;

import com.cleanrepo.account.value_object.UserRole;
import com.cleanrepo.enums.ResponseStatus;

public record SetPasswordResponse(ResponseStatus code,
                                  String message,
                                  String token,
                                  UserRole role) {
}
