package com.mw.KosherChat.model;

import com.mw.KosherChat.app.model.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

  @Id
  public String id;

  @Column(unique = true,length=2048)
  public String tokenValue;

  @Enumerated(EnumType.STRING)
  public TokenType tokenType = TokenType.BEARER;

  public boolean revoked;

  public boolean expired;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "member_id")
  public Member member;

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (this == o) return true;

    if (o instanceof Token that) {
      return this.id != null && Objects.equals(this.id, that.id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Builder
  public static Token fromJwt(Jwt jwt) {
    return Token.builder()
            .id(jwt.getId())
            .expired(false)
            .revoked(false)
            .tokenValue(jwt.getTokenValue())
            .member(Member.builder().id(Long.valueOf(jwt.getSubject())).build())
            .tokenType(TokenType.BEARER)
            .build();
  }
}
