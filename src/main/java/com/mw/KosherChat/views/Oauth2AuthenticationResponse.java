package com.mw.KosherChat.views;

import com.mw.KosherChat.app.model.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Oauth2AuthenticationResponse {
    public Member member;
    public String access_token;
    public String refresh_token;

    @Builder
    public static Oauth2AuthenticationResponse from(TokensResponse tokensResponse, Member member) {
        return Oauth2AuthenticationResponse.builder()
                .member(member)
                .access_token(tokensResponse.access_token)
                .refresh_token(tokensResponse.refresh_token)
                .build();
    }
}
