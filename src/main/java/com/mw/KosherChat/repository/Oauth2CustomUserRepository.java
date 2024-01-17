package com.mw.KosherChat.repository;


import com.mw.KosherChat.model.Oauth2CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Oauth2CustomUserRepository extends JpaRepository<Oauth2CustomUser,Long> {

    Optional<Oauth2CustomUser> findBySub(String sub);
    Optional<Oauth2CustomUser> findByEmail(String email);

}
