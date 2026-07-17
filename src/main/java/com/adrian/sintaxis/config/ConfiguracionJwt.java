package com.adrian.sintaxis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class ConfiguracionJwt {

    private String secret;
    private Long expiration;

    private Blacklist blacklist = new Blacklist();

    @Getter
    @Setter
    public static class Blacklist {
        private boolean enabled = true;
        private Long cleanupInterval = 3600000L;
    }
}
