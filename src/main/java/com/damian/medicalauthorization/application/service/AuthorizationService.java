package com.damian.medicalauthorization.application.service;

import com.damian.medicalauthorization.application.port.AuthorizationUseCase;
import com.damian.medicalauthorization.domain.model.Authorization;
import com.damian.medicalauthorization.infrastructure.persistence.repository.AuthorizationRepository;
import com.damian.medicalauthorization.shared.logging.CorrelationIdHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorizationService implements AuthorizationUseCase {

    private static final String INITIAL_STATUS = "PENDING";

    private final AuthorizationRepository authorizationRepository;

    public AuthorizationService(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    @Override
    public Authorization create(
            String patientDocument,
            String memberNumber,
            String planCode,
            String practiceCode,
            String correlationId
    ) {
        com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization entity =
                new com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization();

        entity.setId(UUID.randomUUID());
        entity.setPatientDocument(patientDocument);
        entity.setMemberNumber(memberNumber);
        entity.setPlanCode(planCode);
        entity.setPracticeCode(practiceCode);
        entity.setStatus(INITIAL_STATUS);
        entity.setCorrelationId(resolveCorrelationId(correlationId));

        return toDomain(authorizationRepository.save(entity));
    }

    @Override
    public Optional<Authorization> findById(UUID authorizationId) {
        return authorizationRepository.findById(authorizationId)
                .map(this::toDomain);
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
