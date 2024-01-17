package com.mw.KosherChat.services;


import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.repositories.MemberRepository;
import com.mw.KosherChat.model.ISSIdentity;
import com.mw.KosherChat.model.Oauth2CustomUser;
import com.mw.KosherChat.model.OauthUsage;
import com.mw.KosherChat.repository.Oauth2CustomUserRepository;
import com.mw.KosherChat.repository.OauthUsageRepository;
import com.mw.KosherChat.views.Oauth2AuthenticationResponse;
import com.mw.KosherChat.views.TokensResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Oauth2CustomUserService {
    @Autowired
    Oauth2CustomUserRepository oauth2Repository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    OauthUsageRepository oauthUsageRepository;


    public Optional<Oauth2CustomUser> findBySub(String sub) {
        return oauth2Repository.findBySub(sub);
    }

    public Optional<Oauth2CustomUser> findByEmail(String email) {
        return oauth2Repository.findByEmail(email);
    }

    public Oauth2CustomUser saveOauth2User(Oauth2CustomUser oauth2CustomUser){
        return oauth2Repository.save(oauth2CustomUser);
    }

    public Oauth2AuthenticationResponse authenticate(JwtAuthenticationToken jwtAuthenticationToken) {
        Oauth2CustomUser oauth2UserFromToken = Oauth2CustomUser.from(jwtAuthenticationToken);
        Oauth2CustomUser oauth2CustomUser = findBySub(oauth2UserFromToken.getSub())
                .orElseGet(() ->
                        saveOauth2User(oauth2UserFromToken));
        oauthUsageRepository.save(OauthUsage.from(oauth2CustomUser));
        return authenticate(oauth2CustomUser);
    }

    public Oauth2AuthenticationResponse authenticate(Oauth2CustomUser oauth2User) {
        //getting member
        String email = oauth2User.getEmail();
        ISSIdentity issIdentity = ISSIdentity.identityFor(oauth2User);
        Member member = memberRepository
                .findByUsername(email)
                .orElseGet(()->{
                    Member newMember = Member.from(oauth2User, issIdentity);
                    newMember = memberRepository.save(newMember);
                    return newMember;
                });

        //getting tokens
        TokensResponse tokens = tokenService.getTokens(member);
        return Oauth2AuthenticationResponse
                .builder()
                .member(member)
                .authorization(tokens)
                .build();

    }

    public long getGoogleUserCount(){
        return oauthUsageRepository.countDistinctSub();
    }

}
