package com.damian.medicalauthorization.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Authorization(
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
