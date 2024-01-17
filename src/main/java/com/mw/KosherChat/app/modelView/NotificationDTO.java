package com.mw.KosherChat.app.modelView;


import com.mw.KosherChat.app.model.Notification;
import com.mw.KosherChat.app.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    Long id;
    NotificationType type;
    Long typeId;
    String message;
    MemberDTO from;
    Long to;
    public LocalDateTime dateTime;
    public boolean enable;

    @Builder
    public static NotificationDTO from(Notification notification){
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
