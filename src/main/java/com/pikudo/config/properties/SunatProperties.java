package com.pikudo.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.sunat")
public class SunatProperties {

    private boolean enabled;
    private String mode = "disabled";
    private String ruc;
    private String solUsername;
    private String solPassword;
    private String pfxBase64;
    private String pfxPassword;
    private String endpointBeta;
    private String endpointProd;

    @PostConstruct
    void validate() {
        if (!enabled) {
            return;
        }
        require(ruc, "SUNAT_RUC");
        require(solUsername, "SUNAT_SOL_USERNAME");
        require(solPassword, "SUNAT_SOL_PASSWORD");
        require(pfxBase64, "SUNAT_PFX_BASE64");
        require(pfxPassword, "SUNAT_PFX_PASSWORD");
    }

    private void require(String value, String envName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(envName + " es requerido cuando SUNAT_ENABLED=true");
        }
    }
}
