package com.mw.KosherChat.repository;

import com.mw.KosherChat.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

  @Query(value = """
      select t from Token t inner join Member u\s
      on t.member.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
  List<Token> findAllValidTokenByMember(Long id);

  Optional<Token> findByTokenValue(String tokenValue);

}
