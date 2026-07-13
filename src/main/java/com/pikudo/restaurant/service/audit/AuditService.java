package com.pikudo.restaurant.service.audit;

import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.audit.AuditLog;
import com.pikudo.restaurant.repository.audit.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(
            String action,
            String outcome,
            Usuario usuario,
            String username,
            HttpServletRequest request,
            String detail
    ) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .usuario(usuario)
                    .username(trim(username, 60))
                    .action(trim(action, 80))
                    .outcome(trim(outcome, 30))
                    .ipAddress(trim(resolveIp(request), 64))
                    .userAgent(trim(request != null ? request.getHeader("User-Agent") : null, 255))
                    .detail(trim(detail, 500))
                    .createdAt(Instant.now())
                    .build());
        } catch (RuntimeException ex) {
            log.warn("No se pudo registrar auditoria {} {}: {}", action, outcome, ex.getMessage());
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
}
