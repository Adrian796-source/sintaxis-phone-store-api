package com.adrian.sintaxis.security;

import com.adrian.sintaxis.config.ConfiguracionJwt;
import com.adrian.sintaxis.exception.TokenInvalidoException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.JwtException;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ConfiguracionJwt configuracionJwt;

    // Almacenamiento en memoria (para producción usar Redis o base de datos)
    private final ConcurrentHashMap<String, Date> blacklist = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    /**
     * Agrega un token a la blacklist
     */
    public void addToBlacklist(String token) {
        // Verificar si la blacklist está habilitada
        if (!configuracionJwt.getBlacklist().isEnabled()) {
            log.info("⚠️ Blacklist deshabilitada. Token no agregado.");
            return;
        }
        try {
            // Extraer fecha de expiración del token
            Claims claims = Jwts.parser()
                    .setSigningKey(configuracionJwt.getSecret())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();

            // Guardar en blacklist hasta que expire
            blacklist.put(token, expiration);

            // Limpiar tokens expirados (opcional pero recomendado)
            cleanExpiredTokens();

            log.info("🔴 Token agregado a blacklist. Expira: {}", expiration);
            log.info("📊 Blacklist size: {}", blacklist.size());
            
        } catch (JwtException e) {
            throw new TokenInvalidoException("Token inválido: " + e.getMessage());
        }
    }

    /**
     * Verifica si un token está en la blacklist
     */
    public boolean isBlacklisted(String token) {
        // Verificar si la blacklist está habilitada
        if (!configuracionJwt.getBlacklist().isEnabled()) {
            return false;
        }

        Date expiration = blacklist.get(token);
        if (expiration == null) {
            return false;
        }

        // Si el token ya expiró, removerlo de la blacklist
        if (expiration.before(new Date())) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }

    /**
     * Limpia tokens expirados de la blacklist (reduce memoria)
     */
    private void cleanExpiredTokens() {
        Date now = new Date();
        int before = blacklist.size();
        blacklist.entrySet().removeIf(entry -> entry.getValue().before(now));
        int after = blacklist.size();
        if (before != after) {
            log.info("🧹 Blacklist limpiada. Removidos: {} tokens expirados", (before - after));
        }
    }

    /**
     * Limpieza automática programada
     */
    @Scheduled(fixedRateString = "${jwt.blacklist.cleanup-interval:3600000}")
    public void scheduledCleanup() {
        if (configuracionJwt.getBlacklist().isEnabled()) {
            cleanExpiredTokens();
            log.info("🧹 Limpieza programada completada. Tamaño actual: {}", blacklist.size());
        }
    }

    /**
     * Obtiene la cantidad de tokens en blacklist (para monitoreo)
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }
}
