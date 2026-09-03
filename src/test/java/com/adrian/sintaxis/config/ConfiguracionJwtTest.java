package com.adrian.sintaxis.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableConfigurationProperties(ConfiguracionJwt.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class ConfiguracionJwtTest {

    @Autowired
    private ConfiguracionJwt configuracionJwt;

    @Test
    void shouldLoadJwtConfig() {
        assertThat(configuracionJwt).isNotNull();
        assertThat(configuracionJwt.getSecret()).isNotNull();
        assertThat(configuracionJwt.getExpiration()).isNotNull();
        assertThat(configuracionJwt.getExpiration()).isPositive();
    }

    @Test
    void shouldHaveValidSecret() {
        String secret = configuracionJwt.getSecret();
        assertThat(secret).isNotNull();
        assertThat(secret.length()).isGreaterThanOrEqualTo(32);
    }

    @Test
    void shouldHaveValidExpiration() {
        Long expiration = configuracionJwt.getExpiration();
        assertThat(expiration)
                .isNotNull()
                .isGreaterThan(0)
                .isGreaterThanOrEqualTo(3600000L); // 1 hora
    }

    @Test
    void shouldHaveBlacklistConfig() {
        ConfiguracionJwt.Blacklist blacklist = configuracionJwt.getBlacklist();
        assertThat(blacklist)
                .isNotNull()
                .satisfies(b -> assertThat(b.isEnabled()).isTrue());
    }

}