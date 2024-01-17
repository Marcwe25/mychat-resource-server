package com.mw.KosherChat.app.controllers;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.model.MemberRoom;
import com.mw.KosherChat.app.model.Room;
import com.mw.KosherChat.app.modelView.MemberRoomRequest;
import com.mw.KosherChat.app.modelView.PostDTO;
import com.mw.KosherChat.app.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final MemberService memberService;
    private final MemberRoomService memberRoomService;
    private final RegisteredMember registeredMember;
    @PutMapping("/{roomId}")
    public void updateroom(@RequestParam long roomId, @RequestBody Room room) {
        room.setId(roomId);
        roomService.saveRoom(room);
    }

    @PutMapping("/addUserToRoom/{roomId}")
    public void addUserToRoom(@PathVariable(name = "roomId") Long roomId, @RequestBody Member member) {

        try {
            Room room = roomService.findRoomById(roomId);
            Member memberToAdd = memberService.findMemberByUsername(member.username);

            memberRoomService.createMemberRoom(memberToAdd,room,false);
        } catch (Exception e) {
            System.err.println("myerror");
            System.err.println(e);
            throw e;
        }
    }

    @PutMapping("/addUsersToRoom/{roomId}")
    public void addUsersToRoom(@PathVariable(name = "roomId") Long roomId, @RequestBody Set<Long> ids) {
        try {
            Room room = roomService.findRoomById(roomId);
            Set<Member> members = new HashSet<>();
            for (Long id : ids) {
                Member memberToAdd = memberService.getMemberById(id);
                members.add(memberToAdd);
            }
            for(Member memberToAdd : members){
                memberRoomService.createMemberRoom(memberToAdd,room,false);

            }

        } catch (Exception e) {
            System.err.println("myerror");
            System.err.println(e);
            throw e;
        }

    }

    @PutMapping("/removeRoomsForMembers")
    public void deleteMemberRooms( @RequestBody Set<Long> roomIds, Authentication authentication) throws Exception {
        Member member = registeredMember.findRegisteredMember(authentication);
        memberRoomService.deleteMemberRooms(roomIds,member);
    }

    @PutMapping("/editUsersInRoom/{roomId}")
    public void editUsersInRoom(@PathVariable(name = "roomId") Long roomId, @RequestBody HashMap<Long,Boolean> ids) {
        try {
            ids
                    .keySet()
                    .stream()
                    .forEach(memberid -> {
                        boolean shouldBeEnable = ids.get(memberid);
                        MemberRoom memberRoom = memberRoomService.getByRoomAndByMember(roomId, memberid);
                        if(memberRoom==null && shouldBeEnable){
                            Member member = memberService.getMemberById(memberid);
                            Room room = roomService.findRoomById(roomId);
                            memberRoomService.createMemberRoom(member,room, true);
                        } else if (memberRoom != null){
                            memberRoom.setDeleted(!shouldBeEnable);
                            memberRoom.setEnable(shouldBeEnable);
                            memberRoomService.save(memberRoom);
                        }
                    });

        } catch (Exception e) {
            System.err.println("myerror");
            System.err.println(e);
            throw e;
        }

    }

    @PostMapping
    public void newroom(@RequestBody MemberRoomRequest memberRoomRequest, Authentication authentication) throws Exception {
        memberRoomService.process(memberRoomRequest,authentication);
    }

    @GetMapping("/{roomId}")
    public Room getroomById(@RequestParam Long roomId) {
        Room room = roomService.findRoomById(roomId);
        return room;
    }

    @GetMapping("/posts/{roomId}")
    public List<PostDTO> findTop10ByRoomIdOrderBydate_time(@PathVariable(name = "roomId") Long roomId, Authentication authentication) throws Exception {
        memberRoomService.setLastSeen(roomId,authentication);
        return roomService.getLast10PostsForRoom(roomId);
    }

    @PostMapping("/lastSeen/{roomId}")
    public ResponseEntity setLastSeen(@PathVariable(name = "roomId") Long roomId, Authentication authentication) throws Exception {
        memberRoomService.setLastSeen(roomId,authentication);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
