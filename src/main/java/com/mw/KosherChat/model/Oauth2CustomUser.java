package com.mw.KosherChat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Oauth2CustomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String iss;
    String sub;
    String given_name;
    String family_name;
    String name;
    String email;
    String picture;

    @Builder
    public static Oauth2CustomUser from(Map<String, Object> credentials){
        return Oauth2CustomUser.builder()
                .iss((String)credentials.get("iss"))
                .sub((String)credentials.get("sub"))
                .given_name((String)credentials.get("given_name"))
                .family_name((String)credentials.get("family_name"))
                .name((String)credentials.get("name"))
                .email((String)credentials.get("email"))
                .picture((String)credentials.get("picture"))
                .build();
    }

    @Builder
    public static Oauth2CustomUser from(JwtAuthenticationToken jwtAuthenticationToken){
        Jwt credentials = (Jwt) jwtAuthenticationToken.getCredentials();
        Map<String, Object> claims = credentials.getClaims();
        Oauth2CustomUser oauth2User = Oauth2CustomUser.from(claims);
        return oauth2User;
    }
}
