package com.mw.KosherChat.services;


import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.repositories.MemberRepository;
import com.mw.KosherChat.app.services.RegisteredMember;
import com.mw.KosherChat.model.ISSIdentity;
import com.mw.KosherChat.model.Token;
import com.mw.KosherChat.repository.TokenRepository;
import com.mw.KosherChat.views.TokensResponse;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {
    SecureRandom secureRandom = new SecureRandom();
    long access_expire = 3;
    long refresh_expire = 36000L;
    RSAPrivateKey privateKey;
    RSAPublicKey publicKey;
    TokenRepository tokenRepository;
    JwtEncoder selfJwtEncoder;
    JwtDecoder selfJwtDecoder;
    MemberRepository memberRepository;
    JwtAuthenticationConverter jwtAuthenticationConverter;
    RegisteredMember registeredMember;

    public TokenService(
            @Value("${jwt.private.key}") RSAPrivateKey privateKey,
            @Value("${jwt.public.key}") RSAPublicKey publicKey,
            TokenRepository tokenRepository,
            MemberRepository memberRepository,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            RegisteredMember registeredMember
    ) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.tokenRepository = tokenRepository;
        this.selfJwtEncoder = getSelfJwtEncoder();
        this.selfJwtDecoder = getSelfJwtDecoder();
        this.memberRepository = memberRepository;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.registeredMember = registeredMember;
    }
    public void revokeAllUserTokens(Member member) {
        var validUserTokens = tokenRepository.findAllValidTokenByMember(member.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    public JwtEncoder getSelfJwtEncoder(){
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
    public JwtDecoder getSelfJwtDecoder(){
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }
    public Member getMember(Token token) throws Exception {

        if (token == null) throw new Exception();
        String cleanedToken = token.getTokenValue();
        Jwt jwt = getJwt(cleanedToken);

        //validation
        String id = jwt.getId();
        Token referenceById = tokenRepository.getReferenceById(id);
        if(referenceById==null) throw new JwtException("token exception");
        if (jwt.getExpiresAt().isBefore(Instant.now())) throw new JwtException("token expired");
        if (token.revoked) throw new JwtException("token revoked");
        String token_userId = jwt.getSubject();
        String iss = jwt.getClaimAsString("iss");

        Member member = registeredMember.findRegisteredMember(token_userId, ISSIdentity.valueOf(iss));
        return member;
    }
    public Jwt getJwt(String token) {
        String cleanedToken = token.replace("Bearer ","");
        Jwt jwt = selfJwtDecoder.decode(cleanedToken);
        Map<String, Object> claims = jwt.getClaims();
        return jwt;
    }
    public Optional<Token> findByToken(String token){
        return tokenRepository.findByTokenValue(token);
    };
    public String getNewJTI(){
        while(true){
            byte[] jtiBytes = new byte[64];
            secureRandom.nextBytes(jtiBytes);
            String jti = Base64.getUrlEncoder().withoutPadding().encodeToString(jtiBytes);
            boolean exist = tokenRepository.existsById(jti);
            if(!exist){
                return jti;
            }
        }
    }
    public JwtClaimsSet getAccessTokenClaims(String memberId){
        String jti = getNewJTI();
        return JwtClaimsSet.builder()
                .id(jti)
                .issuer(ISSIdentity.KCHAT.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(access_expire))
                .subject(memberId)
                .claim("scope", "ROLE_USER")
                .build();
    }
    public JwtClaimsSet getRefreshTokenClaims(String memberId){
        String jti = getNewJTI();
        return JwtClaimsSet.builder()
                .id(jti)
                .issuer(ISSIdentity.KCHAT.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refresh_expire))
                .subject(memberId)
                .claim("scope", "ROLE_USER")
                .build();
    }
    public TokensResponse getTokens(Member member){
//        revokeAllUserTokens(member);
        String memberId = Long.toString(member.getId());

        // generate access token
        JwtClaimsSet accessTokenClaims = getAccessTokenClaims(memberId);
        Jwt accessToken = getJwt(accessTokenClaims);

        // generate refresh token
        JwtClaimsSet refreshTokenClaims = getRefreshTokenClaims(memberId);
        Jwt refreshToken = getJwt(refreshTokenClaims);

        // generate view containing both tokens
        return TokensResponse.builder()
                .access_token(accessToken.getTokenValue())
                .refresh_token(refreshToken.getTokenValue())
                .build();
    }
    public Jwt getJwt(JwtClaimsSet claims){

        Jwt jwt = this.selfJwtEncoder.encode(JwtEncoderParameters.from(claims));
        try {
            Token token = Token.fromJwt(jwt);
            tokenRepository.save(token);

        } catch (Exception e) {
        }
        return jwt;
    }
    public Optional<AbstractAuthenticationToken> getAbstractAuthenticationToken(String token){
        Optional<Token> byToken = this.findByToken(token);
        if(!byToken.isPresent()){return Optional.empty();}
        Jwt jwt = this.getJwt(byToken.get().getTokenValue());
        AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);
        return Optional.of(authenticationToken);
    }

//    public Token validate(String tokenValue){
//        Token token = findByToken(tokenValue).orElseThrow();
//        String foundTokenValue = token.getTokenValue();
//        Jwt decodedToken = selfJwtDecoder.decode(foundTokenValue);
//        selfJwtDecoder.
//        return null;
//    }
}
