package com.cleanrepo.account.dto;

import com.cleanrepo.account.value_object.UserRole;

public record UserDataDto(UserRole appUserRole, String email) {
}
