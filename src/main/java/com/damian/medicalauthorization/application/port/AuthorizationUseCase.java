package com.damian.medicalauthorization.application.port;

import com.damian.medicalauthorization.domain.model.Authorization;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationUseCase {

    Authorization create(Authorization authorization);

    Optional<Authorization> findById(UUID authorizationId);
}
