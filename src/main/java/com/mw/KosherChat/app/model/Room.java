package com.mw.KosherChat.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String name;
    @JsonIgnore
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Set<MemberRoom> memberRooms = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Post> posts = new HashSet<>();
    public boolean enabled = true;

    public void addPost(Post post) {
        this.getPosts().add(post);
        post.setRoom(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setRoom(null);
    }

    public void addMemberRoom(MemberRoom memberRoom) {
        memberRooms.add(memberRoom);
    }

    public void removeMemberRoom(MemberRoom memberRoom) {
        memberRooms.remove(memberRoom);
        memberRoom.setRoom(null);
        memberRoom.setMember(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<MemberRoom> getMemberRooms() {
        return memberRooms;
    }

    public void setMemberRooms(Set<MemberRoom> memberRooms) {
        this.memberRooms = memberRooms;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public void setPosts(Set<Post> posts) {
        this.posts = posts;
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

        Room room = (Room) o;

        return getId() != null && room.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
