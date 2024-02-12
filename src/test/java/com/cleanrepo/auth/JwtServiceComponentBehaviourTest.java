package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.account.value_object.EncodedPassword;
import com.cleanrepo.account.value_object.Locked;
import com.cleanrepo.account.value_object.UserEmail;
import com.cleanrepo.account.value_object.UserRole;
import com.cleanrepo.auth.value_object.Jwt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceComponentBehaviourTest {

    private static JwtService underTest;
    private static AccountCredentials existingAccountCredentials;

    @BeforeAll
    static void setUp() {
        underTest = new JwtService();
        try {
            Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
            secretKeyField.setAccessible(true);
            secretKeyField.set(underTest, "y4mxs9t4kbd782");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            var log = LoggerFactory.getLogger(JwtServiceComponentBehaviourTest.class);
            log.warn("Could not execute test, due to the unsuccessful use of reflection");
        }
        existingAccountCredentials = new AccountCredentials(
                12,
                new UserEmail("test@gmail.com"),
                new EncodedPassword("$10$XGXlxiUEQAUv9c"),
                UserRole.ROLE_USER,
                new Locked(false)
        );
    }

    @Test
    @DisplayName("Should pass when successfully generated jwt for existing credentials")
    void generateJwtWithExistingAccountCredentials() {
        Optional<Jwt> generatedJwt = underTest.generateJwt(existingAccountCredentials);
        assertThat(generatedJwt).isPresent();
        assertThat(generatedJwt.get().jwt()).isNotBlank();
    }

    @Test
    @DisplayName("Should pass when could not generate jwt for not existing credentials")
    void generateJwtWithNotExistingAccountCredentials() {
        Optional<Jwt> generatedJwt = underTest.generateJwt(new AccountCredentials());
        assertThat(generatedJwt).isEmpty();
    }

    @Test
    @DisplayName("Should pass when valid generated jwt has been successfully validated")
    void generateJwtWithExistingAccountCredentialsAndValidate() {
        Optional<Jwt> generatedJwt = underTest.generateJwt(existingAccountCredentials);
        boolean isJwtValid = underTest.validateJwt(existingAccountCredentials, generatedJwt.get());
        assertThat(isJwtValid).isTrue();
    }

    @Test
    @DisplayName("Should pass when invalid generated jwt has been unsuccessfully validated")
    void generateInvalidJwtAndValidateWithExistingAccountCredentials() {
        var invalidJwt = Jwt.from("invalid-jwt");
        boolean isValid = underTest.validateJwt(existingAccountCredentials, invalidJwt);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should pass when successfully extracted account email from valid generated jwt")
    void extractUserEmailFromValidGeneratedJwt() {
        Optional<Jwt> generatedJwt = underTest.generateJwt(existingAccountCredentials);
        Optional<String> userEmail = underTest.extractUserEmailFromJwt(generatedJwt.get());
        assertThat(userEmail).isPresent();
        assertThat(userEmail.get()).isEqualTo("test@gmail.com");
    }

    @Test
    @DisplayName("Should pass when successfully extracted account email from valid generated jwt")
    void extractUserEmailFromInValidGeneratedJwt() {
        Jwt jwt = Jwt.from("invalid-jwt");
        Optional<String> userEmail = underTest.extractUserEmailFromJwt(jwt);
        assertThat(userEmail).isEmpty();
    }
}