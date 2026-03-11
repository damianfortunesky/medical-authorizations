package com.damian.medicalauthorization.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizationCreatedMessage(
        UUID authorizationId,
        String practiceCode,
        String planCode,
        String correlationId
) {
}
