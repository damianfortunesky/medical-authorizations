package com.damian.medicalauthorization.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Authorization(
        UUID id,
        String memberId,
        String providerId,
        String procedureCode,
        String status,
        OffsetDateTime createdAt
) {
}
