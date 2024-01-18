package com.mw.KosherChat.app.services;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.model.MemberRoom;
import com.mw.KosherChat.app.model.Notification;
import com.mw.KosherChat.app.model.NotificationType;
import com.mw.KosherChat.app.modelView.MemberRoomRequest;
import com.mw.KosherChat.app.modelView.NotificationsDTOList;
import com.mw.KosherChat.app.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;
    private final RegisteredMember registeredMember;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate,
                               NotificationRepository notificationRepository,
                               MemberService memberService,
                               RegisteredMember registeredMember
    ) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.memberService = memberService;
        this.registeredMember = registeredMember;
    }

    public Notification newNotification(MemberRoom memberRoom, String message) {
        Notification newContactNotification = Notification.builder()
                .type(NotificationType.NewContact)
                .typeId(memberRoom.getId())
                .from(memberService.getMemberById(memberRoom.getMember().getId()))
                .to(memberRoom.getMember())
                .message("is")
                .dateTime(LocalDateTime.now())
                .enable(true)
                .build();
        notificationRepository.save(newContactNotification);
        return newContactNotification;
    }

    public Notification newNotification(MemberRoomRequest memberRoomRequest, MemberRoom memberRoom) {
        Notification newContactNotification = Notification.builder()
                .type(NotificationType.NewContact)
                .typeId(memberRoom.getId())
                .from(memberService.getMemberById(memberRoomRequest.getFrom()))
                .to(memberRoom.getMember())
                .message(memberRoomRequest.getMessage())
                .dateTime(LocalDateTime.now())
                .enable(true)
                .build();
        notificationRepository.save(newContactNotification);
        return newContactNotification;
    }

    public void newNotificationAndSend(MemberRoomRequest memberRoomRequest, MemberRoom memberRoom) {
        Notification newContactNotification = newNotification(memberRoomRequest, memberRoom);
        String usernameFromIssuer = memberService.getSub(memberRoom.getMember());
        String userId = memberRoom.getMember().getId().toString();

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notification",
                newContactNotification
        );
    }


    public NotificationsDTOList getNotifications(Authentication authentication) throws Exception {
        Member member = registeredMember.findRegisteredMember(authentication);
        List<Notification> notifications = notificationRepository.findAllByTo(member);
        NotificationsDTOList notificationsDTOList = NotificationsDTOList.fromNotifications(notifications);
        return notificationsDTOList;
    }

    public Optional<Notification> getById(long id) {
        return notificationRepository.findById(id);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

}
