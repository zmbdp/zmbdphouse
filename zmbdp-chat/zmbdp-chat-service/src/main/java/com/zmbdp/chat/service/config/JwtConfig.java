package com.zmbdp.chat.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class JwtConfig {

    @Value("${jwt.token.secret}")
    private String secret;

    public String getSecret() {
        return secret;
    }
}

