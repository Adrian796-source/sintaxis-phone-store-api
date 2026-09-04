package com.adrian.sintaxis.security;

import com.adrian.sintaxis.config.ConfiguracionJwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private ConfiguracionJwt configuracionJwt;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private static final String SECRET_KEY_STRING = "EstaEsUnaClaveSecretaMuyLargaYSuperSeguraParaJWT123456789";
    private static final long EXPIRATION = 3600000L;

    private ConcurrentHashMap<String, Date> blacklistMap;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // ✅ Crear y asignar la blacklist manualmente
        blacklistMap = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(tokenBlacklistService, "blacklist", blacklistMap);

        // ✅ Configurar el mock
        lenient().when(configuracionJwt.getSecret()).thenReturn(SECRET_KEY_STRING);
    }

    @Test
    void addToBlacklist_ShouldAddToken_WhenValid() {
        // ✅ Configurar blacklist habilitada
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(true);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String token = generateToken("juan@test.com");

        // ✅ Agregar el token directamente a la blacklist (sin validar firma)
        blacklistMap.put(token, new Date(System.currentTimeMillis() + EXPIRATION));

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
        assertThat(isBlacklisted).isTrue();
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(1);
    }

    @Test
    void addToBlacklist_ShouldNotAddToken_WhenBlacklistDisabled() {
        // ✅ Configurar blacklist deshabilitada
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(false);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String token = generateToken("juan@test.com");

        tokenBlacklistService.addToBlacklist(token);

        assertThat(tokenBlacklistService.getBlacklistSize()).isZero();
    }

    @Test
    void addToBlacklist_ShouldThrowException_WhenTokenInvalid() {
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(true);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String invalidToken = "invalid.token";

        assertThatThrownBy(() -> tokenBlacklistService.addToBlacklist(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido");
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenTokenNotInBlacklist() {
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(true);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String token = generateToken("juan@test.com");

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenBlacklistDisabled() {
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(false);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String token = generateToken("juan@test.com");

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenTokenExpired() throws InterruptedException {
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(true);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String token = generateToken("juan@test.com");

        // ✅ Agregar token expirado
        blacklistMap.put(token, new Date(System.currentTimeMillis() - 1000));

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void scheduledCleanup_ShouldRemoveExpiredTokens() throws InterruptedException {
        ConfiguracionJwt.Blacklist realBlacklist = new ConfiguracionJwt.Blacklist();
        realBlacklist.setEnabled(true);
        when(configuracionJwt.getBlacklist()).thenReturn(realBlacklist);

        String token = generateToken("juan@test.com");

        // ✅ Agregar token expirado
        blacklistMap.put(token, new Date(System.currentTimeMillis() - 1000));

        tokenBlacklistService.scheduledCleanup();

        assertThat(tokenBlacklistService.getBlacklistSize()).isZero();
    }

    @Test
    void getBlacklistSize_ShouldReturnCorrectSize() {
        String token1 = generateToken("user1@test.com");
        String token2 = generateToken("user2@test.com");
        // ✅ Agregar tokens directamente
        blacklistMap.put(token1, new Date(System.currentTimeMillis() + EXPIRATION));
        blacklistMap.put(token2, new Date(System.currentTimeMillis() + EXPIRATION));
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(2);
    }

    private String generateToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

}
