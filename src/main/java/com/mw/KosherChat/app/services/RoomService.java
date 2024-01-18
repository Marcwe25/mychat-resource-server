package com.mw.KosherChat.app.services;


import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.model.Post;
import com.mw.KosherChat.app.model.Room;
import com.mw.KosherChat.app.modelView.PostDTO;
import com.mw.KosherChat.app.repositories.MemberRepository;
import com.mw.KosherChat.app.repositories.MemberRoomRepository;
import com.mw.KosherChat.app.repositories.PostRepository;
import com.mw.KosherChat.app.repositories.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final MemberRoomRepository memberRoomRepository;

    @Autowired
    public RoomService(
            RoomRepository roomRepository,
            MemberRepository memberRepository,
            PostRepository postRepository,
            MemberRoomRepository memberRoomRepository
    ) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.memberRoomRepository = memberRoomRepository;
    }


    public List<Room> createRoom(List<Room> Rooms) {
        List<Room> unsaved = new ArrayList<>();
        for (Room Room : Rooms) {
            roomRepository.save(Room);
        }
        return unsaved;
    }

    public Room saveRoom(Room Room) {

        return roomRepository.save(Room);
    }


    public Room findRoomById(Long id) {
        return roomRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(" room not found"));
    }

    public Post savePost(Post Post) {
        return postRepository.save(Post);
    }

    public boolean disableRoom(long roomId) {
        Room Room;
        Room = roomRepository.findById(roomId).orElseThrow();
        Room.setEnabled(false);
        roomRepository.save(Room);
        return true;
    }

    @Transactional
    public boolean addPostToRoom(Long roomId, Post post) {
        Member member = memberRepository.findByUsername(post.getFrom().getUsername()).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();
        post.setRoom(room);
        post.setFrom(member);
        postRepository.save(post);
        return true;
    }

    public List<PostDTO> getLast10PostsForRoom(long roomId) {
        Sort sort = Sort.by("dateTime").descending();
        return postRepository
                .findPostByRoomId(roomId, sort)
                .stream()
                .map(p -> PostDTO.fromPost(p))
                .collect(Collectors.toList());
    }

}
