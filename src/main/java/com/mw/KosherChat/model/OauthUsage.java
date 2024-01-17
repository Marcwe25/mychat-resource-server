package com.mw.KosherChat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class OauthUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @NotNull
    @ManyToOne
    @JoinColumn(name="oauth2CustomUser_id")
    Oauth2CustomUser oauth2CustomUser;
    LocalDateTime dateTime;

    @Builder
    public static OauthUsage from(Oauth2CustomUser oauth2CustomUser){
        return OauthUsage.builder()
                .dateTime(LocalDateTime.now())
                .oauth2CustomUser(oauth2CustomUser)
                .build();
    }
}
