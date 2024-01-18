package com.mw.KosherChat.controllers;

import com.mw.KosherChat.services.AuthenticationService;
import com.mw.KosherChat.services.Oauth2CustomUserService;
import com.mw.KosherChat.views.AuthenticationRequest;
import com.mw.KosherChat.views.RegisterRequest;
import com.mw.KosherChat.views.TokensResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    Oauth2CustomUserService oauth2CustomUserService;

    @PostMapping("/authenticate")
    public ResponseEntity<TokensResponse> auth(@RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) {
        try {
            return this.authenticationService.authenticate(authenticationRequest);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokensResponse> refreshToken(@CookieValue(name = "refreshToken", required = true) String tokenValue) throws Exception {
        return authenticationService.refreshToken(tokenValue);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count/google")
    public long getGoogleUserCount() {
        return oauth2CustomUserService.getGoogleUserCount();
    }
}