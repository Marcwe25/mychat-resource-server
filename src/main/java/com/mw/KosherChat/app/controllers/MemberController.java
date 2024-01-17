package com.mw.KosherChat.app.controllers;

import com.mw.KosherChat.app.model.*;
import com.mw.KosherChat.app.modelView.*;
import com.mw.KosherChat.app.services.*;
import com.mw.KosherChat.model.ISSIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
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
        RoomDTOMap dtoMap = memberRoomService.getRoomsForUser(member.getId());
        return dtoMap;
    }
    
    @GetMapping
    public Member getRegisteredMember(Authentication authentication) throws Exception{
        Member member = registeredMember.findRegisteredMember(authentication);
        if(member==null) throw new Exception("could not find user as membere");
        return member;
    }


    @PutMapping
    public MemberDTO updateMember(@RequestBody Member member, Authentication authentication) throws Exception {
        Member updatedMember = memberService.updateMember(member,authentication);
        return MemberDTO.from(updatedMember);
    }

    @PutMapping("/unlink/{notificationId}")
    public void unlinkFromRoom(@PathVariable(name = "notificationId") Long notificationId,  Authentication authentication) throws Exception {
        Notification notification = memberRoomService.setUnlinked(notificationId, false, authentication);
//        messagingTemplate.convertAndSendToUser(
//                notification.getTo().getId().toString(),
//                "/queue/contactDecline",
//                NotificationDTO.from(notification)
//        );
        messagingTemplate.convertAndSendToUser(
                notification.getTo().getId().toString(),
                "/queue/notification",
                NotificationDTO.from(notification)
        );
    }

    @PutMapping("/link/{roomId}")
    public Room linkFromRoom(@PathVariable(name = "roomId") Long notificqationId, Authentication authentication) throws Exception {
        MemberRoom memberRoom = memberRoomService.setLinked(notificqationId, true, authentication);
        Room room = memberRoom.getRoom();
        return room;
    }

    @GetMapping("/notifications")
    public NotificationsDTOList getNotifications(Authentication authentication) throws Exception {
        NotificationsDTOList notificationsDTOList = notificationService.getNotifications(authentication);
        return notificationsDTOList;
    }

    @GetMapping("/friends")
    public Map<Long,MemberDTO> getFriends( Authentication authentication) throws Exception {
        Map<Long, MemberDTO> friends = memberService.getFriends(authentication);
        return friends;
    }
}
