package com.cleanrepo.auth.value_object;

import jakarta.validation.constraints.NotBlank;

public record Jwt(

        @NotBlank(message = "Jwt should not be blank")
        String jwt

) {
    public static Jwt from(String jwt) {
        return new Jwt(jwt);
    }
}
