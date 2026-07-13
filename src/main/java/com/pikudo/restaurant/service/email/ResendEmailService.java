package com.pikudo.restaurant.service.email;

import com.pikudo.restaurant.config.properties.ResendProperties;
import com.pikudo.restaurant.exception.BusinessException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.resend.enabled", havingValue = "true")
public class ResendEmailService implements EmailService {

    private final ResendProperties properties;

    @Override
    public void send(EmailMessage message) {
        if (message == null || message.to() == null || message.to().isEmpty()) {
            throw new BusinessException("El email debe tener al menos un destinatario");
        }
        if (!StringUtils.hasText(message.subject())) {
            throw new BusinessException("El email debe tener asunto");
        }
        if (!StringUtils.hasText(message.html()) && !StringUtils.hasText(message.text())) {
            throw new BusinessException("El email debe tener contenido html o texto");
        }

        Resend resend = new Resend(properties.getApiKey());
        CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                .from(properties.getFromEmail())
                .to(message.to())
                .subject(message.subject());

        if (StringUtils.hasText(message.html())) {
            builder.html(message.html());
        }
        if (StringUtils.hasText(message.text())) {
            builder.text(message.text());
        }

        try {
            resend.emails().send(builder.build());
        } catch (ResendException e) {
            throw new BusinessException("No se pudo enviar email con Resend: " + e.getMessage());
        }
    }
}
