package com.mw.KosherChat.app.modelView;

import com.mw.KosherChat.app.model.NotificationType;
import com.mw.KosherChat.app.model.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDTO {

    private Long id;
    public long from;
    public long room;
    public LocalDateTime dateTime;
    public String content;
    public boolean enabled = true;
    public NotificationType type;

    @Builder(builderMethodName = "PostBuilder")
    public static PostDTO fromPost(Post post){
        if(post==null) return null;
        return PostDTO
                .builder()
                .id(post.getId())
                .from(post.getFrom().getId())
                .room(post.getRoom().getId())
                .dateTime(post.getDateTime())
                .content(post.getContent())
                .enabled(post.isEnabled())
                .build();
    }

    @Override
    public String toString() {
        return "PostDTO{" +
                "from=" + from +
                ", room=" + room +
                ", dateTime=" + dateTime +
                ", content='" + content + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
