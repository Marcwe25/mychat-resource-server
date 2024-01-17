package com.mw.KosherChat.model;

import java.util.HashMap;
import java.util.Map;

public enum ISSIdentity {
    GOOGLE("https://accounts.google.com"),
    KCHAT("KCHAT");

    private final String link;
    ISSIdentity(String link) {
        this.link = link;
    }
    private String link () {return link;}
    private static final Map<String,ISSIdentity> MAP = new HashMap<>();
    static {
        for(ISSIdentity identity : values()) {
            MAP.put(identity.link(), identity);
        }
    }
    public static ISSIdentity identityFor(String string) {
        return MAP.get(string);
    }
    public static ISSIdentity identityFor(Oauth2CustomUser oauth2CustomUser) {
        String iss = oauth2CustomUser.getIss();
        ISSIdentity issIdentity = MAP.get(iss);

        return issIdentity;
    }
}
