package com.pikudo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.tracking")
public class TrackingProperties {
    private long locationTtlSeconds = 120;
    private long minIntervalSeconds = 5;
}
