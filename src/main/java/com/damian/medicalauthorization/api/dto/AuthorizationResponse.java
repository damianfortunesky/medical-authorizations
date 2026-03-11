package com.damian.medicalauthorization.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthorizationResponse(
        UUID id,
        String patientDocument,
        String memberNumber,
        String planCode,
        String practiceCode,
        String status,
        String correlationId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
