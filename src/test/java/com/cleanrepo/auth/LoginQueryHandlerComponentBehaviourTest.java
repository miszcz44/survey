package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.account.AccountCredentialsRepository;
import com.cleanrepo.account.value_object.EncodedPassword;
import com.cleanrepo.account.value_object.Locked;
import com.cleanrepo.account.value_object.UserEmail;
import com.cleanrepo.account.value_object.UserRole;
import com.cleanrepo.auth.exception.InvalidPasswordForGivenEmailException;
import com.cleanrepo.auth.exception.LockedAccountException;
import com.cleanrepo.auth.query.LoginQuery;
import com.cleanrepo.auth.value_object.Jwt;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginQueryHandlerComponentBehaviourTest {

    private static BCryptPasswordEncoder bCryptPasswordEncoder;
    private static AuthenticationManager authenticationManager;
    private static AccountCredentialsRepository accountCredentialsRepository;
    private static JwtService jwtService;

    private static LoginQueryHandler underTest;
    private static AccountCredentials existingAccountCredentials;
    private static LoginQuery loginQuery;

    @BeforeAll
    static void setUp() {
        bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        accountCredentialsRepository = mock(AccountCredentialsRepository.class);
        jwtService = mock(JwtService.class);
        underTest = new LoginQueryHandler(bCryptPasswordEncoder, authenticationManager, accountCredentialsRepository, jwtService);
        existingAccountCredentials = new AccountCredentials(
                12,
                new UserEmail("test@gmail.com"),
                new EncodedPassword("$10$XGXlxiUEQAUv9c"),
                UserRole.ROLE_USER,
                new Locked(false)
        );
        loginQuery = new LoginQuery.Json("test@gmail.com", "Password123!").toQuery();
    }

    @Test
    @DisplayName("Should pass when successfully registered account")
    void handleLoginQueryWithValidCredentials() {
        when(accountCredentialsRepository.findByEmail_Email("test@gmail.com"))
                .thenReturn(Optional.of(existingAccountCredentials));
        when(bCryptPasswordEncoder.matches("Password123!", "$10$XGXlxiUEQAUv9c"))
                .thenReturn(true);
        when(jwtService.generateJwt(existingAccountCredentials))
                .thenReturn(Optional.of(new Jwt("valid-jwt")));
        var response = underTest.handleLoginQuery(loginQuery);
        AssertionsForClassTypes.assertThat(response.getJwt().jwt()).isNotBlank();
    }

    @Test
    @DisplayName("Should pass when could not successfully register, due to invalid password")
    void handleLoginQueryWithInvalidPassword() {
        when(accountCredentialsRepository.findByEmail_Email("test@gmail.com"))
                .thenReturn(Optional.of(existingAccountCredentials));
        when(bCryptPasswordEncoder.matches("Password123!", "$10$XGXlxiUEQAUv9c"))
                .thenReturn(false);
        assertThatThrownBy(() -> underTest.handleLoginQuery(loginQuery))
                .isInstanceOf(InvalidPasswordForGivenEmailException.class);
    }

    @Test
    @DisplayName("Should pass when could not successfully register, due to locked account")
    void handleLoginQueryWithAccountLocked() {
        var lockedAccountCredentials = new AccountCredentials(
                13,
                new UserEmail("test@gmail.com"),
                new EncodedPassword("$10$XGXlxiUEQAUv9c"),
                UserRole.ROLE_USER,
                new Locked(true)
        );
        when(accountCredentialsRepository.findByEmail_Email("test@gmail.com"))
                .thenReturn(Optional.of(lockedAccountCredentials));
        when(bCryptPasswordEncoder.matches("Password123!", "$10$XGXlxiUEQAUv9c"))
                .thenReturn(true);
        assertThatThrownBy(() -> underTest.handleLoginQuery(loginQuery))
                .isInstanceOf(LockedAccountException.class);
    }
}