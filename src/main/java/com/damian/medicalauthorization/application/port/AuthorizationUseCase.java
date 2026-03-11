package com.damian.medicalauthorization.application.port;

import com.damian.medicalauthorization.domain.model.Authorization;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationUseCase {

    Authorization create(String patientDocument, String memberNumber, String planCode, String practiceCode, String correlationId);

    Optional<Authorization> findById(UUID authorizationId);
}
