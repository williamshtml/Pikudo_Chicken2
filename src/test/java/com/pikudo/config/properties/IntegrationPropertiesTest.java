package com.pikudo.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrationPropertiesTest {

    @Test
    void storageAllowsGoogleDriveProviderWhenDriveIsDisabled() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider("google-drive");
        properties.getGoogleDrive().setEnabled(false);

        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @Test
    void storageRequiresDriveSecretsWhenDriveIsEnabled() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider("google-drive");
        properties.getGoogleDrive().setEnabled(true);

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRIVE_OAUTH_CLIENT_ID");
    }

    @Test
    void resendDisabledDoesNotRequireApiKey() {
        ResendProperties properties = new ResendProperties();
        properties.setEnabled(false);

        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @Test
    void resendEnabledRequiresApiKey() {
        ResendProperties properties = new ResendProperties();
        properties.setEnabled(true);
        properties.setFromEmail("Pikudo Chicken <no-reply@example.com>");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESEND_API_KEY");
    }

    @Test
    void sunatDisabledDoesNotRequireCertificate() {
        SunatProperties properties = new SunatProperties();
        properties.setEnabled(false);

        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @Test
    void sunatEnabledRequiresRuc() {
        SunatProperties properties = new SunatProperties();
        properties.setEnabled(true);

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUNAT_RUC");
    }
}
