package com.pikudo.security;

import com.pikudo.entity.Usuario;
import com.pikudo.entity.security.RefreshToken;
import com.pikudo.exception.UnauthorizedException;
import com.pikudo.repository.security.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.refresh-token.expiration-millis}")
    private long refreshTokenExpirationMillis;

    public IssuedRefreshToken issue(Usuario usuario, HttpServletRequest request) {
        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);
        Instant now = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .issuedAt(now)
                .expiresAt(now.plusMillis(refreshTokenExpirationMillis))
                .createdByIp(resolveIp(request))
                .userAgent(trim(request != null ? request.getHeader("User-Agent") : null, 255))
                .build();

        refreshTokenRepository.save(refreshToken);
        return new IssuedRefreshToken(rawToken, tokenHash);
    }

    public TokenRotation rotate(String rawToken, HttpServletRequest request) {
        RefreshToken current = requireActive(rawToken);
        IssuedRefreshToken replacement = issue(current.getUsuario(), request);
        current.setRevokedAt(Instant.now());
        current.setReplacedByTokenHash(replacement.tokenHash());
        refreshTokenRepository.save(current);
        return new TokenRotation(current.getUsuario(), replacement.rawToken());
    }

    public Usuario revoke(String rawToken) {
        RefreshToken current = requireActive(rawToken);
        current.setRevokedAt(Instant.now());
        refreshTokenRepository.save(current);
        return current.getUsuario();
    }

    private RefreshToken requireActive(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));
        if (!refreshToken.isActive(Instant.now())) {
            throw new UnauthorizedException("Refresh token expirado o revocado");
        }
        return refreshToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public record IssuedRefreshToken(String rawToken, String tokenHash) {
    }

    public record TokenRotation(Usuario usuario, String rawToken) {
    }
}
