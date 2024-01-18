package com.mw.KosherChat.app.modelView;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
public class RoomDTOMap {
    Map<Long, RoomDTO> rooms = new HashMap();
    Map<Long, PostDTO> lastPosts = new HashMap();
    Map<Long, Integer> unreads = new HashMap();


}
