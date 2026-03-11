package com.damian.medicalauthorization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuthorizationRequest(
        @NotBlank @Size(max = 50) String patientDocument,
        @NotBlank @Size(max = 50) String memberNumber,
        @NotBlank @Size(max = 50) String planCode,
        @NotBlank @Size(max = 50) String practiceCode,
        @Size(max = 100) String correlationId
) {
}
