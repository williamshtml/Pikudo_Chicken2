package com.pikudo.restaurant.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.email.resend")
public class ResendProperties {

    private boolean enabled;
    private String apiKey;
    private String fromEmail;

    @PostConstruct
    void validate() {
        if (!enabled) {
            return;
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("RESEND_API_KEY es requerido cuando RESEND_ENABLED=true");
        }
        if (!StringUtils.hasText(fromEmail)) {
            throw new IllegalStateException("RESEND_FROM_EMAIL es requerido cuando RESEND_ENABLED=true");
        }
    }
}
