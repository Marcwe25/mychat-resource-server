package com.mw.KosherChat.services;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.services.MemberService;
import com.mw.KosherChat.model.Role;
import com.mw.KosherChat.model.Token;
import com.mw.KosherChat.model.User;
import com.mw.KosherChat.views.AuthenticationRequest;
import com.mw.KosherChat.views.RegisterRequest;
import com.mw.KosherChat.views.TokensResponse;
import com.mw.KosherChat.views.VerificationRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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
    MailService mailService;

    public ResponseEntity<TokensResponse> refreshToken(String tokenValue) throws Exception {
        Token token = tokenService.findByToken(tokenValue).orElseThrow();
        Member member = tokenService.getMember(token);
        if (member != null) {
            tokenService.revokeAllUserTokens(member);
        }
        TokensResponse tokensResponse = tokenService.getTokens(member);
        ResponseCookie resCookie = tokenService.refreshTokenAsCookie(tokensResponse.getRefresh_token());
        return ResponseEntity
                .ok()
                .header("Set-Cookie", resCookie.toString())
                .body(tokensResponse);
    }

    public ResponseEntity<TokensResponse> authenticate(AuthenticationRequest authenticationRequest) throws Exception {
        User user = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        if(user==null) throw new UsernameNotFoundException("no user");
        if(user.getRole() != Role.USER) throw new Exception("not a user");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        Member member = memberService.findMemberByUsername(authentication.getName()).orElseThrow();
        TokensResponse tokensResponse = tokenService.getTokens(member);
        ResponseCookie resCookie = tokenService.refreshTokenAsCookie(tokensResponse.getRefresh_token());
        return ResponseEntity
                .ok()
                .header("Set-Cookie", resCookie.toString())
                .body(tokensResponse);
    }

//    public void register(RegisterRequest request) throws Exception {
//        if (validate(request)) throw new Exception("User already registered");
//
//        request.setEmail(cleanString(request.getEmail()));
//        request.setDisplayName(cleanString(request.getDisplayName()));
//        request.setGiven_name(cleanString(request.getGiven_name()));
//        request.setFamily_name(cleanString(request.getFamily_name()));
//        Jwt verifyToken = tokenService.getVerificationToken(member);
//
//
//    }

    public void processVerificationRequest(VerificationRequest verificationRequest) throws Exception {
        if(verificationRequest.getPassword()==null || verificationRequest.getPassword().isBlank()) throw new Exception("incorrect password");
        if(verificationRequest.getEmail()==null || verificationRequest.getEmail().isBlank()) throw new Exception("incorrect email");
        verificationRequest.setEmail(cleanString(verificationRequest.getEmail()));
        String email = verificationRequest.getEmail();
        User user = null;
        if(!userDetailsService.userExist(email)) {
            verificationRequest.setPassword(passwordEncoder.encode(verificationRequest.getPassword()));
            user = User.from(verificationRequest,false);
            userDetailsService.saveUser(user);
        } else {
            user = userDetailsService.loadUserByUsername(email);
        }
        Member member = memberService.getOrNewMemberByUsername(email);

        Jwt verifyToken = tokenService.getVerificationToken(member);
        mailService.sendValidationMessage(user.getEmail(),verifyToken.getTokenValue());
    }

    public void verify(String tokenValue) throws Exception {
        Token token = tokenService.findByToken(tokenValue).orElseThrow();
        Jwt jwt = tokenService.validateToken(token);
        String email = jwt.getClaimAsString("email");
        User user = userDetailsService.loadUserByUsername(email);
        user.setValidated(true);
        user.setRole(Role.USER);
        userDetailsService.saveUser(user);
    }

    public Boolean validate(RegisterRequest request) {
        return userDetailsService.userExist(request.getEmail());
    }


    public static String cleanString(String input) {
        if (StringUtils.isBlank(input)) {return input;}
        // Trim the string using StringUtils
        String trimmedInput = StringUtils.trim(input);
        // Remove invisible control characters and unused code points
        return trimmedInput.replaceAll("\\p{C}", "");
    }


}
