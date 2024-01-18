package com.mw.KosherChat.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "room_id"})})
public class MemberRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "member_id")
    public Member member;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id")
    public Room room;
    LocalDateTime lastSeen;
    @Column(columnDefinition = "boolean default false")
    boolean enable;
    @Column(columnDefinition = "boolean default false")
    boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemberRoom that = (MemberRoom) o;

        if (!getMember().equals(that.getMember())) return false;
        return getRoom().equals(that.getRoom());
    }

    @Override
    public int hashCode() {
        int result = getMember().hashCode();
        result = 31 * result + getRoom().hashCode();
        return result;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    @Override
    public String toString() {
        return "MemberRoom{" +
                "id=" + id +
                ", member=" + member +
                ", room=" + room +
                ", lastSeen=" + lastSeen +
                ", enable=" + enable +
                ", deleted=" + deleted +
                '}';
    }
}
