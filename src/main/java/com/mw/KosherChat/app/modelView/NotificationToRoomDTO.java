package com.mw.KosherChat.app.modelView;

import com.mw.KosherChat.app.model.NotificationToRoom;
import com.mw.KosherChat.app.model.NotificationType;
import com.mw.KosherChat.app.model.Room;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationToRoomDTO {
    Long id;
    NotificationType type;
    Long typeId;
    String message;
    MemberDTO from;
    Room to;
    public LocalDateTime dateTime;
    public boolean enable;

    @Builder
    public static NotificationDTO from(NotificationToRoom notification){
        return NotificationDTO.builder()
                .to(notification.getTo().getId())
                .from(MemberDTO.from(notification.getFrom()))
                .dateTime(notification.getDateTime())
                .type(notification.getType())
                .typeId(notification.getTypeId())
                .id(notification.getId())
                .message(notification.getMessage())
                .enable(notification.isEnable())
                .build();
    }
}
