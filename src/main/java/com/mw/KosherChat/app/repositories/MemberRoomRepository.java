package com.mw.KosherChat.app.repositories;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.model.MemberRoom;
import com.mw.KosherChat.app.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface MemberRoomRepository extends JpaRepository<MemberRoom, Long> {

    List<MemberRoom> findByMemberId(long member_id);

    List<MemberRoom> findByMemberIdAndDeletedFalse(long member_id);

    List<MemberRoom> findByRoomId(long room_id);

    List<MemberRoom> findByRoomIdAndEnableTrue(long room_id);

    List<MemberRoom> findByMemberIdAndEnableTrueAndDeletedFalse(long member_id);

    List<MemberRoom> findByMemberIdAndEnableAndDeleted(long member_id, boolean enable, boolean deleted);

    List<MemberRoom> findByMemberIdAndDeleted(long member_id, boolean deleted);

    MemberRoom findByRoomAndMember(Room room, Member member);

    MemberRoom findByRoomIdAndMemberId(long room_id, long member_id);


    @Transactional
    @Modifying
    @Query(value = "update member_room set enable = false where :room_id = room_id and member_id = :member_id", nativeQuery = true)
    void disableMemberRooms(@Param("member_id") Long member_id, @Param("room_id") Long room_id);

    @Transactional
    @Modifying
    @Query(value = "update member_room set deleted = true, enable = false  where :room_id = room_id and member_id = :member_id", nativeQuery = true)
    void deleteMemberRooms(@Param("member_id") Long member_id, @Param("room_id") Long room_id);


}

