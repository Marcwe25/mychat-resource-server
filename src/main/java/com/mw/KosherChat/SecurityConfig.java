package com.mw.KosherChat;

import com.mw.KosherChat.model.ISSIdentity;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableWebMvc
public class SecurityConfig {

    @Value("${jwt.public.key}")
    RSAPublicKey key;

    @Value("${spring.websecurity.debug:false}")
    boolean webSecurityDebug;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(webSecurityDebug);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain basicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/auth/**")
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();

    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                                authorize
//                                .requestMatchers("/api/v1/auth/**").permitAll()
                                        .requestMatchers("/chat-room-websocket/**").permitAll()
                                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2
                        .authenticationManagerResolver(this.tokenAuthenticationManagerResolver())
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
        return http.build();
    }


    @Bean
    AuthenticationManagerResolver<HttpServletRequest> tokenAuthenticationManagerResolver2() {
        return (request) -> customAuthenticationManagerResolver(request);
    }

    @Bean
    AuthenticationManagerResolver<HttpServletRequest> tokenAuthenticationManagerResolver() {
        return (request) -> {
            Map<String, AuthenticationManager> authenticationManagerMap = new HashMap<>();
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String token = authorizationHeader.replaceAll("^Bearer\\s+", "");
            try {
                String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                if (issuer == null) {
                    throw new RuntimeException();
                }
                if (issuer.equals("KCHAT")) {
                    return jwtSelf();
                }
                authenticationManagerMap.computeIfAbsent(issuer, (iss) -> {
                    JwtDecoder supplierJwtDecoder = new SupplierJwtDecoder(() -> JwtDecoders.fromIssuerLocation(iss));
                    JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(supplierJwtDecoder);
                    jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
                    AuthenticationManager authenticationManager = new ProviderManager(jwtAuthenticationProvider);
                    return authenticationManager;
                });
                return authenticationManagerMap.get(issuer);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public AuthenticationManager jwtSelf() {
        return new ProviderManager(new JwtAuthenticationProvider(NimbusJwtDecoder.withPublicKey(this.key).build()));
    }

    public AuthenticationManager customAuthenticationManagerResolver(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authorizationHeader.replaceAll("^Bearer\\s+", "");
        try {
            String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
            if (issuer != null) {
                if (issuer.equals(ISSIdentity.KCHAT.toString())) {
                    return jwtSelf();
                } else {
                    JwtDecoder supplierJwtDecoder = new SupplierJwtDecoder(() -> JwtDecoders.fromIssuerLocation(issuer));
                    JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(supplierJwtDecoder);
                    jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
                    AuthenticationManager authenticationManager = new ProviderManager(jwtAuthenticationProvider);
                    return authenticationManager;

                }
            }
        } catch (Exception var4) {
            throw new InvalidBearerTokenException(var4.getMessage(), var4);
        }
        throw new InvalidBearerTokenException("Missing issuer");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

}
