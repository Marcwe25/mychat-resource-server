package com.mw.KosherChat;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSocketSecurityConfig implements WebMvcConfigurer {
    protected boolean sameOriginDisabled() {
        return true;
    }

}
