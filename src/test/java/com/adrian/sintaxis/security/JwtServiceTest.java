package com.adrian.sintaxis.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private final String testSecret = "dGVzdHNlY3JldGtleWZvcnRlc3RwdXJwb3Nlc29ubHlzZWN1cmVrZXk=";
    private final Long testExpiration = (Long) 3600000L;  // ✅ Usar Long en lugar de long

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // ✅ Inyectamos valores manualmente
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "expiration", testExpiration);  // ✅ Ahora es Long

        userDetails = User.withUsername("adrian@test.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void generarToken_ShouldCreateValidToken() {
        String token = jwtService.generarToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extraerEmail_ShouldReturnEmailFromToken() {
        String token = jwtService.generarToken(userDetails);
        String email = jwtService.extraerEmail(token);

        assertThat(email).isEqualTo("adrian@test.com");
    }

    @Test
    void esTokenValido_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtService.generarToken(userDetails);
        boolean isValid = jwtService.esTokenValido(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void esTokenValido_ShouldReturnFalse_WhenEmailDoesNotMatch() {
        String token = jwtService.generarToken(userDetails);

        UserDetails otherUser = User.withUsername("otro@test.com")
                .password("password")
                .roles("USER")
                .build();

        boolean isValid = jwtService.esTokenValido(token, otherUser);
        assertThat(isValid).isFalse();
    }

    @Test
    void generarToken_ShouldHaveCorrectExpiration() {
        String token = jwtService.generarToken(userDetails);

        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date expiration = claims.getExpiration();
        Date now = new Date();

        assertThat(expiration).isAfter(now);
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
    }
}