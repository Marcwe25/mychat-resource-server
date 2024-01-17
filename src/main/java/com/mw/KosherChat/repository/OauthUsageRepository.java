package com.mw.KosherChat.repository;

import com.mw.KosherChat.model.OauthUsage;
import com.mw.KosherChat.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OauthUsageRepository extends JpaRepository<OauthUsage, Long> {

    @Query("SELECT count(distinct oauthUsage.oauth2CustomUser) from OauthUsage oauthUsage")
    public int countDistinctSub();
}
