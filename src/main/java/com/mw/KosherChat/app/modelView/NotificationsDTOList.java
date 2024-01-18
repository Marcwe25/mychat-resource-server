package com.mw.KosherChat.app.modelView;


import com.mw.KosherChat.app.model.Notification;
import com.mw.KosherChat.app.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationsDTOList {
    Map<NotificationType, List<NotificationDTO>> notifications;
    List notificationType;

    @Builder(builderMethodName = "grouped")
    public static NotificationsDTOList fromNotifications(List<Notification> notifications) {
        return NotificationsDTOList
                .builder()
                .notifications(
                        notifications
                                .stream()
                                .filter(not -> not.isEnable())
                                .map(not -> NotificationDTO.from(not))
                                .collect(Collectors.groupingBy(
                                        o -> o.getType())))
                .notificationType(Arrays.asList(NotificationType.values()))
                .build();
    }

    @Override
    public String toString() {
        return "NotificationsDTOList{" +
                "notifications=" + notifications +
                ", notificationType=" + notificationType +
                '}';
    }
}
