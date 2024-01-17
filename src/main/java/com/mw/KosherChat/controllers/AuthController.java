package com.mw.KosherChat.controllers;

import com.mw.KosherChat.model.Token;
import com.mw.KosherChat.services.AuthenticationService;
import com.mw.KosherChat.services.Oauth2CustomUserService;
import com.mw.KosherChat.views.AuthenticationRequest;
import com.mw.KosherChat.views.RegisterRequest;
import com.mw.KosherChat.views.TokensResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
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
            TokensResponse tokensResponse = this.authenticationService.authenticate(authenticationRequest);
            ResponseCookie resCookie = ResponseCookie
                    .from("refreshToken", tokensResponse.getRefresh_token())
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(true)
                    .domain("wewehappy.com")
                    .maxAge(Math.toIntExact(60 * 60 * 24 * 7))
                    .build();

            return ResponseEntity
                    .ok()
                    .header("Set-Cookie",  resCookie.toString())
                    .body(tokensResponse);

        }catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public TokensResponse refreshToken(@CookieValue(name = "refreshToken", required = true) String tokenValue) throws Exception {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new Exception("no refresh token ");
        }
        TokensResponse tokensResponse = authenticationService.refreshToken(tokenValue);
        return tokensResponse;

    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count/google")
    public long getGoogleUserCount(){
        return oauth2CustomUserService.getGoogleUserCount();
    }
}