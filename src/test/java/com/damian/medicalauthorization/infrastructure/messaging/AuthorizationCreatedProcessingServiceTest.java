package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization;
import com.damian.medicalauthorization.infrastructure.persistence.entity.AuthorizationStatusHistory;
import com.damian.medicalauthorization.infrastructure.persistence.entity.ProcessedMessage;
import com.damian.medicalauthorization.infrastructure.persistence.repository.AuthorizationRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.AuthorizationStatusHistoryRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationCreatedProcessingServiceTest {

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private AuthorizationStatusHistoryRepository authorizationStatusHistoryRepository;

    private AuthorizationCreatedProcessingService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationCreatedProcessingService(
                processedMessageRepository,
                authorizationRepository,
                authorizationStatusHistoryRepository
        );
    }

    @Test
    void shouldIgnoreAlreadyProcessedMessages() {
        when(processedMessageRepository.existsById("message-1")).thenReturn(true);

        service.process(new AuthorizationCreatedMessage(UUID.randomUUID(), "DENTIST", "SMT01", "corr-1"), "message-1", "corr-1");

        verify(authorizationRepository, never()).findById(any());
        verify(authorizationRepository, never()).save(any());
        verify(authorizationStatusHistoryRepository, never()).save(any());
    }

    @Test
    void shouldTransitionToApprovedAndPersistHistory() {
        UUID authorizationId = UUID.randomUUID();
        Authorization authorization = buildAuthorization(authorizationId, "PENDING", "DENTIST", "SMT01");

        when(processedMessageRepository.existsById("message-2")).thenReturn(false);
        when(authorizationRepository.findById(authorizationId)).thenReturn(Optional.of(authorization));

        service.process(new AuthorizationCreatedMessage(authorizationId, "DENTIST", "SMT01", "corr-2"), "message-2", "corr-2");

        ArgumentCaptor<AuthorizationStatusHistory> historyCaptor = ArgumentCaptor.forClass(AuthorizationStatusHistory.class);
        verify(authorizationStatusHistoryRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(AuthorizationStatusHistory::getFromStatus, AuthorizationStatusHistory::getToStatus)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("PENDING", "VALIDATING"),
                        org.assertj.core.groups.Tuple.tuple("VALIDATING", "APPROVED")
                );

        ArgumentCaptor<ProcessedMessage> processedCaptor = ArgumentCaptor.forClass(ProcessedMessage.class);
        verify(processedMessageRepository).save(processedCaptor.capture());
        assertThat(processedCaptor.getValue().getMessageId()).isEqualTo("message-2");
        assertThat(authorization.getStatus()).isEqualTo("APPROVED");
    }

    private Authorization buildAuthorization(UUID id, String status, String practiceCode, String planCode) {
        Authorization authorization = new Authorization();
        authorization.setId(id);
        authorization.setStatus(status);
        authorization.setPracticeCode(practiceCode);
        authorization.setPlanCode(planCode);
        authorization.setCorrelationId("existing-corr");
        return authorization;
    }
}
