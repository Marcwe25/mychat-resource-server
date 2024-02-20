package com.mw.KosherChat;

import com.nimbusds.jwt.JWTParser;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
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
    Map<String, AuthenticationManager> authenticationManagerMap = new HashMap<>();

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(webSecurityDebug);
    }

    @Bean @Order(1)
    public SecurityFilterChain basicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/auth/**")
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
                ;
        return http.build();

    }

    @Bean @Order(2)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                                authorize
                                        .requestMatchers("/chat-room-websocket/**").permitAll()
                                        .requestMatchers("/api/v1/Oauth2/member/**").authenticated()
                                        .anyRequest().hasAnyAuthority("SCOPE_ROLE_USER")
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
    AuthenticationManagerResolver<HttpServletRequest> tokenAuthenticationManagerResolver() {
        return (request) -> {
            String token = defaultBearerTokenResolver().resolve(request);
            try {
                String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                if (issuer == null) {throw new RuntimeException();}
                authenticationManagerMap.computeIfAbsent(issuer, this::getAuthenticationManager);
                return authenticationManagerMap.get(issuer);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public AuthenticationManager getAuthenticationManager(String iss) {
        JwtDecoder supplierJwtDecoder = new SupplierJwtDecoder(() -> JwtDecoders.fromIssuerLocation(iss));
        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(supplierJwtDecoder);
        jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        AuthenticationManager authenticationManager = new ProviderManager(jwtAuthenticationProvider);
        return authenticationManager;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @PostConstruct
    public void jwtSelf() {
        ProviderManager providerManager = new ProviderManager(new JwtAuthenticationProvider(NimbusJwtDecoder.withPublicKey(this.key).build()));
        authenticationManagerMap.put("KCHAT",providerManager);
    }

    @Bean
    public DefaultBearerTokenResolver defaultBearerTokenResolver(){
        return new DefaultBearerTokenResolver();
    }
}
