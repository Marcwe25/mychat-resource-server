package com.mw.KosherChat.app.services;

import com.mw.KosherChat.app.model.*;
import com.mw.KosherChat.app.modelView.*;
import com.mw.KosherChat.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MemberRoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final MemberService memberService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final RegisteredMember registeredMember;

    @Autowired
    public MemberRoomService(
            RoomRepository roomRepository,
            MemberRepository memberRepository,
            PostRepository postRepository,
            MemberRoomRepository memberRoomRepository,
            MemberService memberService,
            SimpMessagingTemplate messagingTemplate,
            NotificationService notificationService,
            NotificationRepository notificationRepository,
            RegisteredMember registeredMember
    ) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.memberRoomRepository = memberRoomRepository;
        this.memberService = memberService;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.registeredMember = registeredMember;
    }

    public void process(MemberRoomRequest memberRoomRequest, Authentication authentication ) throws Exception {

        Member member = registeredMember.findRegisteredMember(authentication);

        if (member.getUsername() == memberRoomRequest.getTo())
            throw new Exception(member.username);
        Member withMember = memberService.findMemberByUsername(memberRoomRequest.getTo());
        Room room = new Room();
        MemberRoom from = MemberRoom.builder()
                .member(member)
                .room(room)
                .lastSeen(LocalDateTime.now())
                .enable(true)
                .deleted(false)
                .build();
        member.addMemberRoom(from);
        room.addMemberRoom(from);

        MemberRoom to = MemberRoom.builder()
                .member(withMember)
                .room(room)
                .lastSeen(LocalDateTime.now())
                .enable(false)
                .deleted(false)
                .build();
        withMember.addMemberRoom(to);
        room.addMemberRoom(to);


        roomRepository.save(room);
        memberRepository.saveAll(Arrays.asList(member, withMember));
        memberRoomRepository.saveAll(Arrays.asList(from, to));
        notificationService.newNotificationAndSend(memberRoomRequest, to);
    }

    public MemberRoom getBymemberRoomId(Long memberRoomId){
        return memberRoomRepository.findById(memberRoomId).orElse(null);
    }

    public void disableMemberRooms(Set<Long> roomIds,Member registeredMember){
        for(Long roomId : roomIds){
            memberRoomRepository.disableMemberRooms(registeredMember.getId(),roomId);
        }
    }

    public void deleteMemberRooms(Set<Long> roomIds,Member registeredMember){
        for(Long roomId : roomIds){
            memberRoomRepository.deleteMemberRooms(registeredMember.getId(),roomId);
        }
    }
    public MemberRoom getMemberRoom(Member member, Room room) {
        MemberRoom memberRoom = MemberRoom
                .builder()
                .member(member)
                .room(room)
                .lastSeen(LocalDateTime.now())
                .build();
        room.addMemberRoom(memberRoom);
        member.addMemberRoom(memberRoom);
        return memberRoom;
    }

    public MemberRoom createMemberRoom(Member member, Room room, boolean enabled) {
        MemberRoom memberRoom =
                MemberRoom.builder()
                        .member(member)
                        .room(room)
                        .enable(enabled)
                        .build();
        MemberRoom savedMemberRoom = memberRoomRepository.save(memberRoom);
        room.addMemberRoom(savedMemberRoom);
        roomRepository.save(room);
        member.addMemberRoom(savedMemberRoom);
        memberRepository.save(member);

        return savedMemberRoom;
    }

    public MemberRoom getMemberRoomForRegisteredUser(long roomId, Authentication authentication) throws Exception {
        Member member = registeredMember.findRegisteredMember(authentication);
        Room room = roomRepository.getReferenceById(roomId);
        MemberRoom memberRoom = memberRoomRepository.findByRoomAndMember(room, member);
        return memberRoom;
    }

    public MemberRoom getByRoomAndByMember(long roomId, long memberId) {
        MemberRoom memberRoom = memberRoomRepository.findByRoomIdAndMemberId(roomId, memberId);
        return memberRoom;
    }

    public boolean setLastSeen(long roomId, Authentication authentication) throws Exception {
        MemberRoom memberRoom = getMemberRoomForRegisteredUser(roomId,authentication);
        memberRoom.setLastSeen(LocalDateTime.now());
        memberRoomRepository.save(memberRoom);
        return true;
    }

    public boolean save(MemberRoom memberRoom) {
        memberRoomRepository.save(memberRoom);
        return true;
    }

    public List<MemberRoom> getMemberRoomsForRoomId(long roomid) {
        List<MemberRoom> memberRooms = memberRoomRepository.findByRoomId(roomid);
        return memberRooms;
    }

    public List<Long> getMembersIdsForRoomId(long roomid) {
        List<MemberRoom> memberRooms = memberRoomRepository.findByRoomId(roomid);
        List<Long> membersIds = memberRooms
                .parallelStream()
                .map(memberRoom -> memberRoom.getMember().getId())
                .toList();
        return membersIds;
    }


    public RoomDTOMap getRoomsForUser(long registeredUserId) {
        Map<Long, LocalDateTime> lastSeen = new HashMap<>();
        Map<Long, MemberDTO> memberDTOs = new HashMap<>();
        Map<Long, PostDTO> lastPostsDTO = new HashMap<>();
        Map<Long, Integer> unreads = new HashMap<>();

        Map<Long, RoomDTO> roomDTOs =
                memberRoomRepository
                        .findByMemberIdAndDeleted(registeredUserId,false)
                        .parallelStream()
                        .peek(mr -> lastSeen.putIfAbsent(mr.getRoom().getId(), mr.getLastSeen()))
                        .map(mr -> mr.getRoom().getId())
                        .peek(roomId -> lastPostsDTO.putIfAbsent(
                                roomId,PostDTO.fromPost(
                                        postRepository
                                                .findFirstByRoomOrderByDateTimeDesc(
                                                        roomRepository.getReferenceById(roomId)
                                                ))
                        ))
                        .peek(roomId -> unreads
                                .putIfAbsent(roomId,
                                        postRepository
                                        .countByRoomIdAndDateTimeIsAfter(roomId, lastSeen.get(roomId))
                                )
                        )
                        .collect(Collectors.toMap(Function.identity(),

                                        roomId -> {
                                            List<MemberRoom> memberRooms = memberRoomRepository.findByRoomId(roomId);
                                            memberRooms.parallelStream().map(memberRoom -> memberRoom.getMember().getId())
                                                    .toList();
                                            return RoomDTO
                                                    .builder()
                                                    .id(roomId)
                                                    .members(memberRooms
                                                            .parallelStream()
                                                            .map(memberRoom -> memberRoom.getMember().getId())
                                                            .toList())
                                                    .enabled(
                                                            memberRooms.stream()
                                                                    .filter(m->m.getMember().getId()!=registeredUserId)
                                                                    .anyMatch(m->m.isEnable())
                                                    )
                                                    .build();
                                        }

                                )
                        );

        return RoomDTOMap.builder()
                .rooms(roomDTOs)
                .lastPosts(lastPostsDTO)
                .unreads(unreads)
                .build();
    }

    public MemberRoom setLinked(long notificqationId, boolean isLinked,  Authentication authentication) throws Exception {
        Optional<Notification> opNotification = notificationRepository.findById(notificqationId);
        Notification notification = opNotification.get();
        NotificationType type = notification.getType();
        Member member = registeredMember.findRegisteredMember(authentication);
        if (type == NotificationType.NewContact) {
            Optional<MemberRoom> opMemberRoom = memberRoomRepository.findById(notification.getTypeId());
            MemberRoom memberRoom = opMemberRoom.get();
            memberRoom.setEnable(true);
            memberRoom.setDeleted(false);
            memberRoomRepository.save(memberRoom);
            notification.setEnable(false);
            notificationRepository.save(notification);

            memberRoomRepository
                    .findByRoomId(memberRoom.getRoom().getId())
                    .parallelStream()
                    .filter(mr -> mr.getMember().getId()==member.getId())
                    .forEach(mr -> {
                        Notification n = Notification.builder()
                                .to(mr.getMember())
                                .from(member)
                                .type(NotificationType.ContactAccept)
                                .typeId(notificqationId)
                                .enable(true)
                                .message(mr.getRoom().getId().toString())
                                .dateTime(LocalDateTime.now())
                                .build();
                        messagingTemplate.convertAndSendToUser(
                                notification.getTo().getId().toString(),
                                "/queue/notification",
                                NotificationDTO.from(n)
                        );

                    });

            return memberRoom;
        }
        return null;
    }

    public Notification setUnlinked(long notificationId, boolean isLinked,  Authentication authentication) throws Exception {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        NotificationType type = notification.getType();
        if (type == NotificationType.NewContact) {
            MemberRoom memberRoom = memberRoomRepository.findById(notification.getTypeId()).orElseThrow();
            memberRoom.setEnable(false);
            memberRoom.setDeleted(true);
            memberRoomRepository.save(memberRoom);
            notification.setEnable(false);
            notificationRepository.save(notification);

            Member responseToMember = notification.getFrom();
            Room room = memberRoom.getRoom();
            MemberRoom responseMemberRoom = memberRoomRepository.findByRoomAndMember(room,responseToMember);
            responseMemberRoom.setEnable(false);
            responseMemberRoom.setDeleted(true);
            save(responseMemberRoom);
            Member from = registeredMember.findRegisteredMember(authentication);
            Member to = responseMemberRoom.getMember();

            Notification responseNotification = Notification.builder()
                    .to(to)
                    .from(from)
                    .message(room.getId().toString())
                    .type(NotificationType.ContactDecline)
                    .typeId(notificationId)
                    .dateTime(LocalDateTime.now())
                    .enable(false)
                    .build();
            responseNotification = notificationService.save(responseNotification);
            return responseNotification;
        }
        return null;
    }

}
