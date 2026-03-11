package com.damian.medicalauthorization.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizationResultMessage(
        UUID authorizationId,
        String status,
        String patientDocument,
        String memberNumber,
        String planCode,
        String practiceCode,
        String correlationId,
        OffsetDateTime updatedAt
) {
}
