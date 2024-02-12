package com.cleanrepo.auth;

import com.cleanrepo.account.AccountCredentials;
import com.cleanrepo.account.AccountCredentialsRepository;
import com.cleanrepo.auth.exception.InvalidJwtException;
import com.cleanrepo.auth.exception.UserEmailNotFoundException;
import com.cleanrepo.auth.value_object.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

@Component
@Profile("!without-security")
@RequiredArgsConstructor
class LoginFilterConfig extends OncePerRequestFilter {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;
    private final AccountCredentialsRepository accountCredentialsRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            System.out.println("doFilterInternal");
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (!validateAuthHeader(request, response, filterChain, authHeader)) return;

            Jwt jwtFromHeader = Jwt.from(authHeader.substring(7));
            Optional<String> jwtEmail = jwtService.extractUserEmailFromJwt(jwtFromHeader);
            if(!jwtEmail.isEmpty()) {

                var userDetails = this.userDetailsService.loadUserByUsername(jwtEmail.get());
                var accountCredentials = getUserCredentials(jwtEmail.get());

                if (!jwtService.validateJwt(accountCredentials, jwtFromHeader))
                    throw new InvalidJwtException();
                setAuthTokenToSecurityContext(request, userDetails, accountCredentials);

                filterChain.doFilter(request, response);
            }
        } catch(InvalidJwtException e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    private boolean validateAuthHeader(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain,
                                       String authHeader
    ) throws ServletException, IOException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return false;
        }
        return true;
    }

    private static void setAuthTokenToSecurityContext(HttpServletRequest request,
                                                      UserDetails userDetails,
                                                      AccountCredentials accountCredentials
    ) {
        var authToken = new UsernamePasswordAuthenticationToken(accountCredentials, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private AccountCredentials getUserCredentials(String email) {
        return accountCredentialsRepository
                .findByEmail_Email(email)
                .orElseThrow(UserEmailNotFoundException::new);
    }
}
