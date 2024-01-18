package com.mw.KosherChat.app.controllers;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.model.MemberRoom;
import com.mw.KosherChat.app.model.Notification;
import com.mw.KosherChat.app.model.Room;
import com.mw.KosherChat.app.modelView.MemberDTO;
import com.mw.KosherChat.app.modelView.NotificationDTO;
import com.mw.KosherChat.app.modelView.NotificationsDTOList;
import com.mw.KosherChat.app.modelView.RoomDTOMap;
import com.mw.KosherChat.app.services.MemberRoomService;
import com.mw.KosherChat.app.services.MemberService;
import com.mw.KosherChat.app.services.NotificationService;
import com.mw.KosherChat.app.services.RegisteredMember;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRoomService memberRoomService;
    private final NotificationService notificationService;
    private final RegisteredMember registeredMember;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/rooms")
    public RoomDTOMap getRoomsForUser(Authentication authentication) throws Exception {
        Member member = registeredMember.findRegisteredMember(authentication);
        return memberRoomService.getRoomsForUser(member.getId());
    }

    @GetMapping
    public Member getRegisteredMember(Authentication authentication) throws Exception {
        Member member = registeredMember.findRegisteredMember(authentication);
        if (member == null) throw new Exception("could not find user as member");
        return member;
    }


    @PutMapping
    public MemberDTO updateMember(@RequestBody Member member, Authentication authentication) throws Exception {
        Member updatedMember = memberService.updateMember(member, authentication);
        return MemberDTO.from(updatedMember);
    }

    @PutMapping("/unlink/{notificationId}")
    public void unlinkFromRoom(@PathVariable(name = "notificationId") Long notificationId, Authentication authentication) throws Exception {
        Notification notification = memberRoomService.setUnlinked(notificationId, false, authentication);
        messagingTemplate.convertAndSendToUser(
                notification.getTo().getId().toString(),
                "/queue/notification",
                NotificationDTO.from(notification)
        );
    }

    @PutMapping("/link/{roomId}")
    public Room linkFromRoom(@PathVariable(name = "roomId") Long notificationId, Authentication authentication) throws Exception {
        MemberRoom memberRoom = memberRoomService.setLinked(notificationId, true, authentication);
        return memberRoom.getRoom();
    }

    @GetMapping("/notifications")
    public NotificationsDTOList getNotifications(Authentication authentication) throws Exception {
        return notificationService.getNotifications(authentication);
    }

    @GetMapping("/friends")
    public Map<Long, MemberDTO> getFriends(Authentication authentication) throws Exception {
        return memberService.getFriends(authentication);
    }
}
