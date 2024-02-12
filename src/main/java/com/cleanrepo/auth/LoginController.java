package com.cleanrepo.auth;

import com.cleanrepo.auth.query.LoginQuery;
import com.cleanrepo.auth.query.LoginQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin
class LoginController {

    private final LoginQueryHandler loginQueryHandler;

    @Value("${security.jwt.cookieName}")
    private String cookieName;

    @PostMapping("/api/login")
    @CrossOrigin(origins = {"http://localhost:5173"})
    ResponseEntity<Void> loginUser(@RequestBody LoginQuery.Json requestJson) {
        var loginQuery = requestJson.toQuery();
        LoginQueryResponse response = loginQueryHandler.handleLoginQuery(loginQuery);
        var responseJson = LoginQueryResponse.Json.fromQuery(response);

        HttpCookie cookie = ResponseCookie.from(cookieName, responseJson.jwt()).path("/").build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
