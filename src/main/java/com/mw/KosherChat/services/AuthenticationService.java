package com.mw.KosherChat.services;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.services.MemberService;
import com.mw.KosherChat.model.Role;
import com.mw.KosherChat.model.Token;
import com.mw.KosherChat.model.User;
import com.mw.KosherChat.views.AuthenticationRequest;
import com.mw.KosherChat.views.RegisterRequest;
import com.mw.KosherChat.views.TokensResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    private final AuthenticationManager authenticationManager;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    TokenService tokenService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MemberService memberService;
    @Autowired
    Oauth2CustomUserService oauth2CustomUserService;

    public TokensResponse refreshToken(String tokenValue) throws Exception {
        Token token = tokenService.findByToken(tokenValue).orElseThrow();
        Member member = tokenService.getMember(token);
        if(member!=null) {tokenService.revokeAllUserTokens(member);}
        return tokenService.getTokens(member);
    }

    public TokensResponse authenticate(AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        Member member = memberService.findMemberByUsername(authentication.getName());
        return tokenService.getTokens(member);
    }

    public void register(RegisterRequest request) {
        if(validate(request));
        request.setEmail(cleanString(request.getEmail()));
        request.setDisplayName(cleanString(request.getDisplayName()));
        request.setGiven_name(cleanString(request.getGiven_name()));
        request.setFamily_name(cleanString(request.getFamily_name()));
        Member member = Member.from(request);
        memberService.createMember(member);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);
        User user = User.from(request);
        userDetailsService.saveUser(user);
    }
    public Boolean validate(RegisterRequest request){
        User userFetched = userDetailsService.loadUserByUsername(request.getEmail());
        if(userFetched==null) {
            return false;
        }
        if(request.getRole()==null) {
            request.setRole(Role.USER);
        }
        return true;
    }


    public static String cleanString(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        // Trim the string using StringUtils
        String trimmedInput = StringUtils.trim(input);

        // Remove invisible control characters and unused code points
        return trimmedInput.replaceAll("\\p{C}", "");
    }

}
