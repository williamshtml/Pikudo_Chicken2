package com.pikudo.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String provider = "local";
    private Local local = new Local();
    private GoogleDrive googleDrive = new GoogleDrive();

    @PostConstruct
    void validate() {
        if (!StringUtils.hasText(provider)) {
            throw new IllegalStateException("APP_STORAGE_PROVIDER no puede estar vacio");
        }

        if ("local".equalsIgnoreCase(provider) && !StringUtils.hasText(local.basePath)) {
            throw new IllegalStateException("APP_STORAGE_LOCAL_BASE_PATH es requerido cuando APP_STORAGE_PROVIDER=local");
        }

        if (googleDrive.enabled) {
            require(googleDrive.oauthClientId, "DRIVE_OAUTH_CLIENT_ID");
            require(googleDrive.oauthClientSecret, "DRIVE_OAUTH_CLIENT_SECRET");
            require(googleDrive.oauthRefreshToken, "DRIVE_OAUTH_REFRESH_TOKEN");
        }
    }

    private void require(String value, String envName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(envName + " es requerido cuando Google Drive storage esta habilitado");
        }
    }

    @Getter
    @Setter
    public static class Local {
        private String basePath = "uploads";
    }

    @Getter
    @Setter
    public static class GoogleDrive {
        private boolean enabled;
        private String oauthClientId;
        private String oauthClientSecret;
        private String oauthRefreshToken;
        private Folders folders = new Folders();
    }

    @Getter
    @Setter
    public static class Folders {
        private String products;
        private String avatarUsers;
        private String deliveryEvidence;
        private String sunatRoot;
    }
}
