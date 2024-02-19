package com.mw.KosherChat.controllers;

import com.mw.KosherChat.services.AuthenticationService;
import com.mw.KosherChat.services.Oauth2CustomUserService;
import com.mw.KosherChat.views.AuthenticationRequest;
import com.mw.KosherChat.views.TokensResponse;
import com.mw.KosherChat.views.VerificationRequest;
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


    @CrossOrigin
    @PostMapping("/authenticate")
    public ResponseEntity<TokensResponse> auth(@RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) throws Exception {
        return this.authenticationService.authenticate(authenticationRequest);

    }

    @CrossOrigin
    @PostMapping("/refresh-token")
    public ResponseEntity<TokensResponse> refreshToken(@CookieValue(name = "refreshToken", required = true) String tokenValue) throws Exception {
        return authenticationService.refreshToken(tokenValue);
    }

    @CrossOrigin
    @PostMapping("/verificationRequest")
    public ResponseEntity verify(@RequestBody VerificationRequest verificationRequest) throws Exception {
        authenticationService.processVerificationRequest(verificationRequest);
        return ResponseEntity.ok().build();
    }
    @CrossOrigin(originPatterns = {"/**"})
    @GetMapping("/verify")
    public ResponseEntity verify(@RequestHeader String Authorization) throws Exception {
        authenticationService.verify(Authorization);
        return ResponseEntity.ok().build();
    }
    @CrossOrigin
    @GetMapping("/count/google")
    public long getGoogleUserCount() {
        return oauth2CustomUserService.getGoogleUserCount();
    }
}