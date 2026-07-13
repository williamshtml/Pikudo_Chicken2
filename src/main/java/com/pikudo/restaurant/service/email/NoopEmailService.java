package com.pikudo.restaurant.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.resend.enabled", havingValue = "false", matchIfMissing = true)
public class NoopEmailService implements EmailService {

    @Override
    public void send(EmailMessage message) {
        log.debug("EmailService deshabilitado. Mensaje omitido para {}", message.to());
    }
}
