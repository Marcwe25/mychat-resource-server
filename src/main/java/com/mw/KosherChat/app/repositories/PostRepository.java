package com.mw.KosherChat.app.repositories;



import com.mw.KosherChat.app.model.Post;
import com.mw.KosherChat.app.model.Room;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    public int countByRoomIdAndDateTimeIsAfter(long roomId,LocalDateTime dateTime);

    public List<Post> findPostByRoomId(long roomId, Sort sort);
    public Post findFirstByRoomOrderByDateTimeAsc(Room room);
    public Post findFirstByRoomOrderByDateTimeDesc(Room room);

}
