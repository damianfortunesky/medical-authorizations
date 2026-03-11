package com.damian.medicalauthorization.application.service;

import com.damian.medicalauthorization.application.port.AuthorizationUseCase;
import com.damian.medicalauthorization.domain.model.Authorization;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEvent;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEventStatus;
import com.damian.medicalauthorization.infrastructure.persistence.repository.AuthorizationRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.OutboxEventRepository;
import com.damian.medicalauthorization.shared.logging.CorrelationIdHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorizationService implements AuthorizationUseCase {

    private static final String INITIAL_STATUS = "PENDING";
    private static final String AUTHORIZATION_CREATED_EVENT_TYPE = "AuthorizationCreated";
    private static final String AUTHORIZATION_AGGREGATE_TYPE = "Authorization";

    private final AuthorizationRepository authorizationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public AuthorizationService(
            AuthorizationRepository authorizationRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.authorizationRepository = authorizationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Authorization create(
            String patientDocument,
            String memberNumber,
            String planCode,
            String practiceCode,
            String correlationId
    ) {
        String resolvedCorrelationId = resolveCorrelationId(correlationId);

        com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization entity =
                new com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization();

        entity.setId(UUID.randomUUID());
        entity.setPatientDocument(patientDocument);
        entity.setMemberNumber(memberNumber);
        entity.setPlanCode(planCode);
        entity.setPracticeCode(practiceCode);
        entity.setStatus(INITIAL_STATUS);
        entity.setCorrelationId(resolvedCorrelationId);

        com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization savedAuthorization =
                authorizationRepository.save(entity);

        outboxEventRepository.save(buildAuthorizationCreatedOutboxEvent(savedAuthorization));

        return toDomain(savedAuthorization);
    }

    @Override
    public Optional<Authorization> findById(UUID authorizationId) {
        return authorizationRepository.findById(authorizationId)
                .map(this::toDomain);
    }

    private OutboxEvent buildAuthorizationCreatedOutboxEvent(
            com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization authorization
    ) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setAggregateId(authorization.getId());
        outboxEvent.setAggregateType(AUTHORIZATION_AGGREGATE_TYPE);
        outboxEvent.setEventType(AUTHORIZATION_CREATED_EVENT_TYPE);
        outboxEvent.setPayloadJson(buildAuthorizationCreatedPayload(authorization));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setRetryCount(0);
        outboxEvent.setCorrelationId(authorization.getCorrelationId());
        return outboxEvent;
    }

    private String buildAuthorizationCreatedPayload(
            com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization authorization
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("authorizationId", authorization.getId());
        payload.put("patientDocument", authorization.getPatientDocument());
        payload.put("memberNumber", authorization.getMemberNumber());
        payload.put("planCode", authorization.getPlanCode());
        payload.put("practiceCode", authorization.getPracticeCode());
        payload.put("status", authorization.getStatus());
        payload.put("correlationId", authorization.getCorrelationId());
        payload.put("createdAt", authorization.getCreatedAt());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event payload", e);
        }
    }

    private String resolveCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }

        String mdcCorrelationId = CorrelationIdHolder.getOrDefault();
        if (!"N/A".equals(mdcCorrelationId)) {
            return mdcCorrelationId;
        }

        return UUID.randomUUID().toString();
    }

    private Authorization toDomain(com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization entity) {
        return new Authorization(
                entity.getId(),
                entity.getPatientDocument(),
                entity.getMemberNumber(),
                entity.getPlanCode(),
                entity.getPracticeCode(),
                entity.getStatus(),
                entity.getCorrelationId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
