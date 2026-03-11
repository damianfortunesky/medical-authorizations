package com.damian.medicalauthorization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuthorizationRequest(
        @NotBlank @Size(max = 50) String memberId,
        @NotBlank @Size(max = 50) String providerId,
        @NotBlank @Size(max = 30) String procedureCode
) {
}
