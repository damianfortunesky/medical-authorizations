package com.damian.medicalauthorization.application.service;

import com.damian.medicalauthorization.application.port.AuthorizationUseCase;
import com.damian.medicalauthorization.domain.model.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class StubAuthorizationService implements AuthorizationUseCase {

    private static final Logger log = LoggerFactory.getLogger(StubAuthorizationService.class);
    private static final String STATUS_RECEIVED = "RECEIVED";

    @Override
    public Authorization create(Authorization authorization) {
        UUID generatedId = UUID.randomUUID();
        log.info("Stub create authorization called with memberId={}, providerId={}, procedureCode={}",
                authorization.memberId(), authorization.providerId(), authorization.procedureCode());

        return new Authorization(
                generatedId,
                authorization.memberId(),
                authorization.providerId(),
                authorization.procedureCode(),
                STATUS_RECEIVED,
                OffsetDateTime.now()
        );
    }

    @Override
    public Optional<Authorization> findById(UUID authorizationId) {
        log.info("Stub findById called for authorizationId={}", authorizationId);
        return Optional.empty();
    }
}
