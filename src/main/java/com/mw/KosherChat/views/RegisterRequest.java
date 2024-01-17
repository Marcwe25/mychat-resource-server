package com.mw.KosherChat.views;

import com.mw.KosherChat.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    public String displayName;
    public String given_name;
    public String family_name;
    private String email;
    private String password;

    private Role role;
}
