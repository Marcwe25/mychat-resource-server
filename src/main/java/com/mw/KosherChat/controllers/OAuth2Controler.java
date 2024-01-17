package com.mw.KosherChat.controllers;

import com.mw.KosherChat.services.Oauth2CustomUserService;
import com.mw.KosherChat.views.Oauth2AuthenticationResponse;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/Oauth2/member")
public class OAuth2Controler {
    Oauth2CustomUserService oauth2Service;
    @Autowired
    public OAuth2Controler(Oauth2CustomUserService oauth2CustomUserService) {
        this.oauth2Service = oauth2CustomUserService;
    }

    @GetMapping("/google")
    public Oauth2AuthenticationResponse googleAuthentication(JwtAuthenticationToken authentication) {
        return oauth2Service.authenticate(authentication);
    }

    @Data
    @Accessors(chain = true)
    private static class Info {
        private String application;
        private Map<String, Object> principal;
    }
}
