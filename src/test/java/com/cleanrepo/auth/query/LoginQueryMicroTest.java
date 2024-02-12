package com.cleanrepo.auth.query;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoginQueryMicroTest {

    @Test
    @DisplayName("Should create LoginQuery with all valid data")
    void createFromValidData() {
        assertDoesNotThrow(() -> new LoginQuery.Json(
                "test@gmail.com",
                "Password123!"
        ).toQuery());
    }

    @Test
    @DisplayName("Should not create LoginQuery with invalid email")
    void createFromInvalidEmail() {
        assertThatThrownBy( () -> new LoginQuery.Json(
                "test-email-without-at-symbol",
                "Password123!"
        ).toQuery())
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("format");
    }

    @Test
    @DisplayName("Should not create LoginQuery with blank fields")
    void createFromBlankFields() {
        assertThatThrownBy( () -> new LoginQuery.Json(
                "test@gmail.com",
                ""
        ).toQuery())
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("blank");
    }
}