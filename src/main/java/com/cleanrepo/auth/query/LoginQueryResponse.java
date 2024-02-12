package com.cleanrepo.auth.query;

import com.cleanrepo.auth.value_object.Jwt;
import com.cleanrepo.common.SelfValidating;
import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public final class LoginQueryResponse extends SelfValidating<LoginQueryResponse> {

    @Valid private final Jwt jwt;

    private LoginQueryResponse(Jwt jwt) {
        this.jwt = jwt;
        validateSelf();
    }

    public static LoginQueryResponse from(Jwt jwt) {
        return new LoginQueryResponse(jwt);
    }

    public record Json(String jwt) {
        public static LoginQueryResponse.Json fromQuery(LoginQueryResponse result) {
            return new LoginQueryResponse.Json(result.getJwt().jwt());
        }
    }
}
