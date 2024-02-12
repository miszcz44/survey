package com.cleanrepo.auth.query;

import com.cleanrepo.auth.value_object.Jwt;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoginQueryResponseMicroTest {

    @Test
    @DisplayName("Should create LoginQueryResponse with valid jwt structure")
    void createFromValidData() {
        assertDoesNotThrow(() -> LoginQueryResponse.from(
                new Jwt("eyJhbGciOsInR5cCI6IkJ9.eyJzdWIiOiIxMjMiaWF0NTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QOk6yJV_ad5c")
        ));
    }

    @Test
    @DisplayName("Should not create LoginQueryResponse with blank jwt")
    void createFromInvalidEmail() {
        assertThatThrownBy( () -> LoginQueryResponse.from(
                new Jwt("")
        ))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("blank");
    }

    @Test
    @DisplayName("Should create json from LoginQueryResponse with valid value")
    void createJsonFromValidData() {
        var loginQuery = LoginQueryResponse.from(
                new Jwt("eyJhbGciOsInR5cCI6IkJ9.eyJzdWIiOiIxMjMiaWF0NTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QOk6yJV_ad5c"));
        var loginQueryJson = LoginQueryResponse.Json.fromQuery(loginQuery);
        Assertions.assertThat(loginQuery.getJwt().jwt()).isEqualTo(loginQueryJson.jwt());
    }

}