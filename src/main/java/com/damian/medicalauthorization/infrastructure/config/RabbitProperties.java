package com.damian.medicalauthorization.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging.rabbit")
public record RabbitProperties(
        boolean enabled,
        String exchange,
        String authorizationCreatedRoutingKey
) {
}
