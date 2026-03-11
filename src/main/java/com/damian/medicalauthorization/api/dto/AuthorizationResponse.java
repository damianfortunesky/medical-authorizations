package com.damian.medicalauthorization.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthorizationResponse(
        UUID id,
        String memberId,
        String providerId,
        String procedureCode,
        String status,
        OffsetDateTime createdAt
) {
}
