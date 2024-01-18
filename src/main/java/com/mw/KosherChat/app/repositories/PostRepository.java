package com.mw.KosherChat.app.repositories;


import com.mw.KosherChat.app.model.Post;
import com.mw.KosherChat.app.model.Room;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    int countByRoomIdAndDateTimeIsAfter(long roomId, LocalDateTime dateTime);

    List<Post> findPostByRoomId(long roomId, Sort sort);

    Post findFirstByRoomOrderByDateTimeAsc(Room room);

    Post findFirstByRoomOrderByDateTimeDesc(Room room);

}
