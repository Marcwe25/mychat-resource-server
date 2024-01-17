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
    Member member;
    TokensResponse authorization;
}
