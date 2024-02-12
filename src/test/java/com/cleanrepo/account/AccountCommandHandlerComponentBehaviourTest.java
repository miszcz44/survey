package com.cleanrepo.account;

import com.cleanrepo.account.command.RegisterCommand;
import com.cleanrepo.account.exception.EmailTakenException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountCommandHandlerComponentBehaviourTest {

    private static AccountCommandHandler underTest;
    private static AccountRepository accountRepository;
    private static BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeAll
    static void setUp() {
        accountRepository = mock(AccountRepository.class);
        bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
        underTest = new AccountCommandHandler(accountRepository, bCryptPasswordEncoder);
    }


    @Test
    @DisplayName("Should pass when given user successfully registered account")
    void handleNewRegisterAccount() {
        var registerCommand = new RegisterCommand.Json(
                "test@gmail.com",
                "Password123!").toCommand();
        when(accountRepository.findByEmail_Email("test@gmail.com"))
                .thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(registerCommand.getPassword().getRawPassword()))
                .thenReturn("$10$XGXlxiUEQAUv9c");
        underTest.handleRegisterAdmin(registerCommand);
        verify(accountRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when given user email has already registered account")
    void handleAlreadyRegisteredAccount() {
        var registerCommand = new RegisterCommand.Json(
                "test@gmail.com",
                "Password123!").toCommand();
        when(accountRepository.findByEmail_Email("test@gmail.com"))
                .thenReturn(Optional.of(new Account(registerCommand, "$10$XGXlxiUEQAUv9c")));
        assertThatThrownBy(() -> underTest.handleRegisterAdmin(registerCommand)).isInstanceOf(EmailTakenException.class);
    }

}