package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.account.AccountCredentialsRepository;
import com.cleanrepo.auth.exception.InvalidJwtException;
import com.cleanrepo.auth.exception.InvalidPasswordForGivenEmailException;
import com.cleanrepo.auth.exception.LockedAccountException;
import com.cleanrepo.auth.exception.UserEmailNotFoundException;
import com.cleanrepo.auth.query.LoginQuery;
import com.cleanrepo.auth.query.LoginQueryResponse;
import com.cleanrepo.auth.value_object.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
 class LoginQueryHandler {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccountCredentialsRepository accountCredentialsRepository;
    private final JwtService jwtService;

    public LoginQueryResponse handleLoginQuery(LoginQuery loginQuery) throws LockedAccountException {
        var accountCredentials = getAccountCredentials(loginQuery);
        log.info("User is trying to log in, userId: {}", accountCredentials.getAccountId());

        if(!isUserPasswordValid(loginQuery, accountCredentials)) throw new InvalidPasswordForGivenEmailException();
        if(accountCredentials.isAccountLocked()) throw new LockedAccountException();
        log.info("User credentials are valid, userId: {}", accountCredentials.getAccountId());

        var jwt = generateJwtFromCredentials(accountCredentials);
        log.info("Successfully generated jwt for user, userId: {}", accountCredentials.getAccountId());

        authenticateToContext(loginQuery.getEmail().getEmail(), loginQuery.getPassword().getRawPassword());
        log.info("User has successfully logged in, userId: {}", accountCredentials.getAccountId());

        return LoginQueryResponse.from(jwt);
    }

    private Jwt generateJwtFromCredentials(AccountCredentials accountCredentials) {
        System.out.println("generateJwtFromCredentials");
        System.out.println(accountCredentials);
        return jwtService
                .generateJwt(accountCredentials)
                .orElseThrow(InvalidJwtException::new);
    }

    private AccountCredentials getAccountCredentials(LoginQuery loginQuery) {
        return accountCredentialsRepository
                .findByEmail_Email(loginQuery.getEmail().getEmail())
                .orElseThrow(UserEmailNotFoundException::new);
    }

    private void authenticateToContext(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private boolean isUserPasswordValid(LoginQuery loginQuery, AccountCredentials accountCredentials) {
        return bCryptPasswordEncoder.matches(
                loginQuery.getPassword().getRawPassword(),
                accountCredentials.getEncodedPassword().getEncodedPassword());
    }
}
