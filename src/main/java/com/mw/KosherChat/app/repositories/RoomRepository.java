package com.mw.KosherChat.app.repositories;

import com.mw.KosherChat.app.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {


    List<Room> findByIdAndEnabledTrue(long id);


}
