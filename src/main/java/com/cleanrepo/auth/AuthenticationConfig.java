package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.account.AccountCredentialsRepository;
import com.cleanrepo.auth.exception.UserEmailNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.Collection;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
class AuthenticationConfig {

    private final AccountCredentialsRepository accountCredentialsRepository;

    @Value("${security.jwt.cookieName}")
    private String cookieName;

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> buildUserDetailsFromCredentials(
                accountCredentialsRepository
                        .findByEmail_Email(email)
                        .orElseThrow(UserEmailNotFoundException::new)
        );
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return;
            SecurityContextHolder.clearContext();
            HttpCookie cookie = ResponseCookie.from(cookieName, "").path("/").build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        };
    }

    private UserDetails buildUserDetailsFromCredentials(@Valid AccountCredentials accountCredentials) {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton((GrantedAuthority) () -> accountCredentials.getRole().name());
            }
            @Override
            public String getPassword() {
                return accountCredentials.getEncodedPassword().getEncodedPassword();
            }

            @Override
            public String getUsername() {
                return accountCredentials.getEmail().getEmail();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return !accountCredentials.getLocked().isLocked();
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() { return true; }
        };
    }
}
