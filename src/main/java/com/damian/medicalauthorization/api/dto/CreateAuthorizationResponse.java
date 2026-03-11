package com.damian.medicalauthorization.api.dto;

import java.util.UUID;

public record CreateAuthorizationResponse(
        UUID authorizationId,
        String correlationId,
        String status
) {
}
