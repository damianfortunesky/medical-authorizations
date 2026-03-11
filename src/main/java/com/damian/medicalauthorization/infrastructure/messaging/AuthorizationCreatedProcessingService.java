package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization;
import com.damian.medicalauthorization.infrastructure.persistence.entity.AuthorizationStatusHistory;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEvent;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEventStatus;
import com.damian.medicalauthorization.infrastructure.persistence.entity.ProcessedMessage;
import com.damian.medicalauthorization.infrastructure.persistence.repository.AuthorizationRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.AuthorizationStatusHistoryRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.OutboxEventRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.ProcessedMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthorizationCreatedProcessingService {

    private static final String EVENT_TYPE = "AuthorizationCreated";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_VALIDATING = "VALIDATING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_WAITING_AUDIT = "WAITING_AUDIT";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String PRACTICE_DENTIST = "DENTIST";
    private static final String AUTHORIZATION_AGGREGATE_TYPE = "Authorization";
    private static final String EVENT_TYPE_AUTHORIZATION_APPROVED = "AuthorizationApproved";
    private static final String EVENT_TYPE_AUTHORIZATION_REJECTED = "AuthorizationRejected";
    private static final String EVENT_TYPE_AUTHORIZATION_REQUIRES_AUDIT = "AuthorizationRequiresAudit";
    private static final String PRACTICE_SURGERY_01 = "SURGERY01";
    private static final String PLAN_SMT_01 = "SMT01";

    private final ProcessedMessageRepository processedMessageRepository;
    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationStatusHistoryRepository authorizationStatusHistoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public AuthorizationCreatedProcessingService(
            ProcessedMessageRepository processedMessageRepository,
            AuthorizationRepository authorizationRepository,
            AuthorizationStatusHistoryRepository authorizationStatusHistoryRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.processedMessageRepository = processedMessageRepository;
        this.authorizationRepository = authorizationRepository;
        this.authorizationStatusHistoryRepository = authorizationStatusHistoryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process(AuthorizationCreatedMessage message, String messageId, String correlationId) {
        if (processedMessageRepository.existsById(messageId)) {
            return;
        }

        UUID authorizationId = message.authorizationId();
        if (authorizationId == null) {
            throw new IllegalArgumentException("AuthorizationCreated payload is missing authorizationId");
        }
        Authorization authorization = authorizationRepository.findById(authorizationId)
                .orElseThrow(() -> new IllegalStateException("Authorization not found for id " + authorizationId));

        if (STATUS_PENDING.equals(authorization.getStatus())) {
            transitionStatus(authorization, STATUS_VALIDATING, correlationId);
        }

        String targetStatus = resolveTargetStatus(authorization.getPracticeCode(), authorization.getPlanCode());
        if (!targetStatus.equals(authorization.getStatus())) {
            transitionStatus(authorization, targetStatus, correlationId);
        }

        Authorization savedAuthorization = authorizationRepository.save(authorization);
        outboxEventRepository.save(buildResultOutboxEvent(savedAuthorization));
        processedMessageRepository.save(buildProcessedMessage(messageId, correlationId));
    }

    private void transitionStatus(Authorization authorization, String toStatus, String correlationId) {
        AuthorizationStatusHistory history = new AuthorizationStatusHistory();
        history.setId(UUID.randomUUID());
        history.setAuthorizationId(authorization.getId());
        history.setFromStatus(authorization.getStatus());
        history.setToStatus(toStatus);
        history.setCorrelationId(correlationId);

        authorization.setStatus(toStatus);
        authorizationStatusHistoryRepository.save(history);
    }


    private OutboxEvent buildResultOutboxEvent(Authorization authorization) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setAggregateId(authorization.getId());
        outboxEvent.setAggregateType(AUTHORIZATION_AGGREGATE_TYPE);
        outboxEvent.setEventType(resolveResultEventType(authorization.getStatus()));
        outboxEvent.setPayloadJson(buildResultPayload(authorization));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setRetryCount(0);
        outboxEvent.setCorrelationId(authorization.getCorrelationId());
        return outboxEvent;
    }

    private String buildResultPayload(Authorization authorization) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("authorizationId", authorization.getId());
        payload.put("status", authorization.getStatus());
        payload.put("patientDocument", authorization.getPatientDocument());
        payload.put("memberNumber", authorization.getMemberNumber());
        payload.put("planCode", authorization.getPlanCode());
        payload.put("practiceCode", authorization.getPracticeCode());
        payload.put("correlationId", authorization.getCorrelationId());
        payload.put("updatedAt", authorization.getUpdatedAt());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize authorization result payload", e);
        }
    }

    private String resolveResultEventType(String status) {
        return switch (status) {
            case STATUS_APPROVED -> EVENT_TYPE_AUTHORIZATION_APPROVED;
            case STATUS_REJECTED -> EVENT_TYPE_AUTHORIZATION_REJECTED;
            case STATUS_WAITING_AUDIT -> EVENT_TYPE_AUTHORIZATION_REQUIRES_AUDIT;
            default -> throw new IllegalStateException("Unsupported final status for result event: " + status);
        };
    }

    private ProcessedMessage buildProcessedMessage(String messageId, String correlationId) {
        ProcessedMessage processedMessage = new ProcessedMessage();
        processedMessage.setMessageId(messageId);
        processedMessage.setEventType(EVENT_TYPE);
        processedMessage.setCorrelationId(correlationId);
        return processedMessage;
    }

    private String resolveTargetStatus(String practiceCode, String planCode) {
        if (PRACTICE_DENTIST.equals(practiceCode) && PLAN_SMT_01.equals(planCode)) {
            return STATUS_APPROVED;
        }
        if (PRACTICE_SURGERY_01.equals(practiceCode)) {
            return STATUS_WAITING_AUDIT;
        }
        return STATUS_REJECTED;
    }
}
