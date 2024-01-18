package com.mw.KosherChat.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mw.KosherChat.model.ISSIdentity;
import com.mw.KosherChat.model.Oauth2CustomUser;
import com.mw.KosherChat.model.Token;
import com.mw.KosherChat.model.User;
import com.mw.KosherChat.views.RegisterRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    @NotBlank(message = "username is mandatory")
    public String username;
    public String displayName;
    public String given_name;
    public String family_name;
    public String pictureUrl;

    @Enumerated(EnumType.STRING)
    public ISSIdentity iss;
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @JsonIgnore
    public Set<MemberRoom> memberRooms = new HashSet<>();

    @OneToMany(mappedBy = "from", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public Set<Post> chatPosts = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private List<Token> tokens;

    boolean enabled = true;

    @Builder(builderMethodName = "modelBuilder")
    public static Member from(User user) {
        return Member.builder()
                .username(user.getEmail())
                .build();
    }

    @Builder
    public static Member from(RegisterRequest registerRequest) {
        return Member.builder()
                .username(registerRequest.getEmail())
                .displayName(registerRequest.getDisplayName())
                .family_name(registerRequest.getFamily_name())
                .given_name(registerRequest.getGiven_name())
                .iss(ISSIdentity.KCHAT)
                .enabled(true)
                .build();
    }

    @Builder(builderMethodName = "fromOauth2CustomUser")
    public static Member from(Oauth2CustomUser oauth2CustomUser, ISSIdentity issIdentity) {
        return Member
                .builder()
                .username(oauth2CustomUser.getEmail())
                .given_name(oauth2CustomUser.getName())
                .family_name(oauth2CustomUser.getFamily_name())
                .pictureUrl(oauth2CustomUser.getPicture())
                .iss(issIdentity)
                .build();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ISSIdentity getIss() {
        return iss;
    }

    public void setIss(ISSIdentity iss) {
        this.iss = iss;
    }

    public void addMemberRoom(MemberRoom memberRoom) {
        memberRooms.add(memberRoom);
    }

    public void removeMemberRoom(MemberRoom memberRoom) {
        memberRooms.remove(memberRoom);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Set<MemberRoom> getMemberRooms() {
        return memberRooms;
    }

    public void setMemberRooms(Set<MemberRoom> memberRooms) {
        this.memberRooms = memberRooms;
    }

    public Set<Post> getChatPosts() {
        return chatPosts;
    }

    public void setChatPosts(Set<Post> chatPosts) {
        this.chatPosts = chatPosts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;

        return getUsername().equals(member.getUsername());
    }

    @Override
    public int hashCode() {
        return getUsername().hashCode();
    }
}
