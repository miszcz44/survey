package com.cleanrepo.account.command;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RegisterCommandMicroTest {

    @Test
    @DisplayName("Should create RegisterCommand with all valid data")
    void createFromValidData() {
        assertDoesNotThrow(() -> new RegisterCommand.Json(
                "test@gmail.com",
                "Password123!").toCommand());
    }

    @Test
    @DisplayName("Should not create RegisterCommand with invalid email")
    void createFromInvalidEmail() {
        assertThatThrownBy( () -> new RegisterCommand.Json(
                "test-email-without-at-symbol",
                "Password123!"
        ).toCommand())
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("format");
    }

    @Test
    @DisplayName("Should not create RegisterCommand with blank fields")
    void createFromBlankFields() {
        assertThatThrownBy( () -> new RegisterCommand.Json(
                "test@gmail.com",
                ""
        ).toCommand())
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("blank");
    }
}