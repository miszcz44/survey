package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.account.value_object.EncodedPassword;
import com.cleanrepo.account.value_object.Locked;
import com.cleanrepo.account.value_object.UserEmail;
import com.cleanrepo.account.value_object.UserRole;
import com.cleanrepo.auth.exception.AttemptOfAuthenticateNotAuthenticatedEndpoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootApplication(scanBasePackages = "com.cleanrepo.auth")
class AuthenticationFacadeDependencyTest {

    private static AuthenticationFacade underTest = new AuthenticationFacade();
    private static AccountCredentials existingAccountCredentials;

    @BeforeAll
    static void setUp() {
        underTest = new AuthenticationFacade();
        existingAccountCredentials = new AccountCredentials(
                12,
                new UserEmail("test@gmail.com"),
                new EncodedPassword("$10$XGXlxiUEQAUv9c"),
                UserRole.ROLE_USER,
                new Locked(false)
        );
    }

    @Test
    @DisplayName("Should pass when successfully got account id from security context")
    void getExistingAccountIdFromSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(existingAccountCredentials, "$10$XGXlxiUEQAUv9c");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThat(underTest.getAccountId()).isEqualTo(12);
    }

    @Test
    @DisplayName("Should pass when successfully got account email from security context")
    void getExistingAccountEmailFromSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(existingAccountCredentials, "$10$XGXlxiUEQAUv9c");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThat(underTest.getAccountEmail()).isEqualTo("test@gmail.com");
    }

    @Test
    @DisplayName("Should throw exception when not found existing account id in security context")
    void getAccountIdWithInvalidPrincipal() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("not-existing-principal", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatThrownBy( () -> underTest.getAccountId())
                .isInstanceOf(AttemptOfAuthenticateNotAuthenticatedEndpoint.class);

    }

    @Test
    @DisplayName("Should throw exception when not found existing account email in security context")
    void getAccountEmailWithInvalidPrincipal() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("not-existing-principal", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatThrownBy( () -> underTest.getAccountEmail())
                .isInstanceOf(AttemptOfAuthenticateNotAuthenticatedEndpoint.class);
    }
}