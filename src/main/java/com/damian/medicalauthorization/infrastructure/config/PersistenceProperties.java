package com.damian.medicalauthorization.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.persistence")
public record PersistenceProperties(
        int defaultQueryTimeoutSeconds
) {
}
