package com.mw.KosherChat.app.controllers;

import com.mw.KosherChat.app.model.*;
import com.mw.KosherChat.app.modelView.NotificationDTO;
import com.mw.KosherChat.app.modelView.NotificationToRoomDTO;
import com.mw.KosherChat.app.modelView.PostDTO;
import com.mw.KosherChat.app.services.MemberRoomService;
import com.mw.KosherChat.app.services.RegisteredMember;
import com.mw.KosherChat.app.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final MemberRoomService memberRoomService;

    private final RegisteredMember registeredMember;

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload Post post) throws Exception {
        List<MemberRoom> memberRooms = memberRoomService.getMemberRoomsForRoomId(roomId);
        roomService.addPostToRoom(roomId, post);
        PostDTO postDTO = PostDTO.fromPost(post);
        messagingTemplate.convertAndSend("/topic/" + roomId, postDTO);
    }

    @MessageMapping("/request/{roomId}")
    public void requestResponse(@DestinationVariable Long roomId, @Payload Boolean response, Authentication authentication) throws Exception {
        if (response) {
            Member member = registeredMember.findRegisteredMember(authentication);
            String postMessage = member.getUsername() + " accepted, you're now connected";
            PostDTO postDTO = PostDTO
                    .builder()
                    .room(roomId)
                    .content(postMessage)
                    .dateTime(LocalDateTime.now())
                    .from(member.getId())
                    .build();
            MemberRoom memberRoom = memberRoomService.getByRoomAndByMember(roomId, member.getId());
            memberRoom.setEnable(true);
            messagingTemplate.convertAndSend("/topic/" + roomId, postDTO);
        } else {
            Member member = registeredMember.findRegisteredMember(authentication);
            String postMessage = member.getUsername() + " declined, you're not connected";
            PostDTO postDTO = PostDTO
                    .builder()
                    .room(roomId)
                    .content(postMessage)
                    .dateTime(LocalDateTime.now())
                    .from(member.getId())
                    .build();
            MemberRoom memberRoom = memberRoomService.getByRoomAndByMember(roomId, member.getId());
            memberRoom.setEnable(false);
            messagingTemplate.convertAndSend("/topic/" + roomId, postDTO);
        }
    }

    @MessageMapping("/{roomId}incoming")
    public void newMessageNothification(@DestinationVariable Long roomId, @Payload Post post) {
        roomService.addPostToRoom(roomId, post);
        messagingTemplate.convertAndSend("/topic/" + roomId, post);
    }

    @MessageMapping("/istyping/{roomId}")
    public void typingNotification(@DestinationVariable Long roomId, @Payload Post post) throws Exception {
        Room room = roomService.findRoomById(roomId);
        NotificationToRoom notification = NotificationToRoom
                .builder()
                .type(NotificationType.isTyping)
                .typeId(roomId)
                .to(room)
                .message("istyping")
                .from(post.from)
                .dateTime(LocalDateTime.now())
                .enable(true)
                .build();

        NotificationDTO notificationDTO = NotificationToRoomDTO.from(notification);

        messagingTemplate.convertAndSend("/topic/" + roomId, notificationDTO);
    }

    @MessageMapping("/contactDecline/{roomId}")
    public void contactDeclineNotification(@DestinationVariable Long roomId, @Payload Post post) throws Exception {
        Room room = roomService.findRoomById(roomId);
        NotificationToRoom notification = NotificationToRoom
                .builder()
                .type(NotificationType.isTyping)
                .typeId(roomId)
                .to(room)
                .message("istyping")
                .from(post.from)
                .dateTime(LocalDateTime.now())
                .enable(true)
                .build();

        NotificationDTO notificationDTO = NotificationToRoomDTO.from(notification);

        messagingTemplate.convertAndSend("/topic/" + roomId, notificationDTO);
    }
}
