package com.mw.KosherChat.app.services;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.repositories.MemberRepository;
import com.mw.KosherChat.model.ISSIdentity;
import com.mw.KosherChat.model.Oauth2CustomUser;
import com.mw.KosherChat.repository.Oauth2CustomUserRepository;
import com.mw.KosherChat.services.Oauth2CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class RegisteredMember {

//    @Autowired
//    MemberService memberService;
//    @Autowired
//    Oauth2CustomUserService oauth2CustomUserService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    Oauth2CustomUserRepository oauth2CustomUserRepository;

    public Member findRegisteredMember(Authentication authentication) throws Exception {
        return findRegisteredMember((JwtAuthenticationToken) authentication);
    }

    public Member findRegisteredMember(OAuth2AuthenticatedPrincipal oAuth2AuthenticatedPrincipal) throws Exception {

        return findRegisteredMember((JwtAuthenticationToken) oAuth2AuthenticatedPrincipal);
    }

    public Member findRegisteredMember(JwtAuthenticationToken authenticationToken) throws Exception {
        String subject = authenticationToken.getToken().getSubject();
        ISSIdentity issuer = ISSIdentity.identityFor(authenticationToken.getToken().getClaimAsString("iss"));
        return findRegisteredMember(subject,issuer);
    }

    public Member findRegisteredMember(String subject,ISSIdentity issuer) {
        switch (issuer) {
            case KCHAT : {
                Long userid = Long.valueOf(subject);
                Member memberById = memberRepository.findById(userid).orElseThrow();
                return memberById;

            }
            case GOOGLE : {
                Oauth2CustomUser user = oauth2CustomUserRepository.findBySub(subject).orElseThrow();
                String userEmail = user.getEmail();
                Member member = memberRepository.findByUsername(userEmail).orElseThrow();
                return member;
            }
            default : throw new JwtException("could not locate user");
        }
    }
}
