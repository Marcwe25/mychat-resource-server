package com.mw.KosherChat.app.modelView;


import com.mw.KosherChat.app.model.MemberRoom;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberRoomRequest {

    Long from;
    String to;
    String message;
    LocalDateTime issued;

}
