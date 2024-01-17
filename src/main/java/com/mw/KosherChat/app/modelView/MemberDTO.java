package com.mw.KosherChat.app.modelView;

import com.mw.KosherChat.app.model.Member;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberDTO {

    private Long id;
    public String username;
    public String displayName;
    public String given_name;
    public String family_name;
    public String pictureUrl;
    public String iss;

    @Builder(builderMethodName = "memberBuilder")
    public static MemberDTO from(Member member){
        return MemberDTO
                .builder()
                .id(member.getId())
                .username(member.getUsername())
                .displayName(member.getDisplayName())
                .given_name(member.getGiven_name())
                .family_name(member.getFamily_name())
                .pictureUrl(member.getPictureUrl())
                .iss(member.getIss().toString())
                .build();
    }

}
