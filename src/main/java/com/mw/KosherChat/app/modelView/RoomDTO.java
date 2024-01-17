package com.mw.KosherChat.app.modelView;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class RoomDTO {

    private Long id;
    public String name;
    public List<Long> members;
    public boolean enabled;

}
