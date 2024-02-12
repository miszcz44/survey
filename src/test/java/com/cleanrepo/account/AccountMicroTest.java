package com.cleanrepo.account;

import com.cleanrepo.account.command.RegisterCommand;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountMicroTest {

    @Test
    @DisplayName("Newly created account should not be locked by default")
    void createFromValidData() {
        RegisterCommand registerCommand = new RegisterCommand.Json(
                "test@gmail.com",
                "Password123!").toCommand();
        var newAccount = new Account(registerCommand, "$10$XGXlxiUEQAUv9c");
        Assertions.assertThat(newAccount.getLocked().isLocked()).isFalse();
    }
}