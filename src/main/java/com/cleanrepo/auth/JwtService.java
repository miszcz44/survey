package com.cleanrepo.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.auth.value_object.Jwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class JwtService{

    @Value("${security.jwt.secretKey}")
    private String secretKey;

    public Optional<Jwt> generateJwt(AccountCredentials accountCredentials) {
        try {
            String jwt = JWT.create()
                    .withSubject(accountCredentials.getEmail().getEmail())
                    .withClaim("role", accountCredentials.getRole().name())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusSeconds(60 * 60 * 24)) //24h
                    .sign(Algorithm.HMAC256(secretKey));
            return Optional.of(Jwt.from(jwt));
        } catch (JWTCreationException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    public boolean validateJwt(AccountCredentials accountCredentials, Jwt jwt) {
        try {
            JWTVerifier verifier = JWT
                    .require(Algorithm.HMAC256(secretKey))
                    .withSubject(accountCredentials.getEmail().getEmail())
                    .withClaim("role", accountCredentials.getRole().name())
                    .build();
            verifier.verify(jwt.jwt());
            return true;
        } catch (JWTVerificationException exception){
            return false;
        }
    }

    public Optional<String> extractUserEmailFromJwt(Jwt jwt) {
        try {
            return Optional.of(JWT.decode(jwt.jwt()).getSubject());
        } catch(JWTDecodeException e) {
            return Optional.empty();
        }
    }
}
