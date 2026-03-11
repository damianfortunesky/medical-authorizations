package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.infrastructure.persistence.entity.Notification;
import com.damian.medicalauthorization.infrastructure.persistence.entity.ProcessedMessage;
import com.damian.medicalauthorization.infrastructure.persistence.repository.NotificationRepository;
import com.damian.medicalauthorization.infrastructure.persistence.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class AuthorizationResultNotificationProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationResultNotificationProcessingService.class);
    private final ProcessedMessageRepository processedMessageRepository;
    private final NotificationRepository notificationRepository;

    public AuthorizationResultNotificationProcessingService(
            ProcessedMessageRepository processedMessageRepository,
            NotificationRepository notificationRepository
    ) {
        this.processedMessageRepository = processedMessageRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void process(AuthorizationResultMessage message, String messageId, String eventType, String correlationId) {
        if (processedMessageRepository.existsById(messageId)) {
            return;
        }

        logger.info(
                "Simulating notification send for authorizationId={} eventType={} status={} correlationId={}",
                message.authorizationId(),
                eventType,
                message.status(),
                correlationId
        );

        notificationRepository.save(buildNotification(message, eventType, correlationId));
        processedMessageRepository.save(buildProcessedMessage(messageId, eventType, correlationId));
    }

    private Notification buildNotification(AuthorizationResultMessage message, String eventType, String correlationId) {
        if (message.authorizationId() == null) {
            throw new IllegalArgumentException("Authorization result payload is missing authorizationId");
        }

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setAuthorizationId(message.authorizationId());
        notification.setEventType(eventType);
        notification.setStatus(message.status());
        notification.setPatientDocument(message.patientDocument());
        notification.setMemberNumber(message.memberNumber());
        notification.setPlanCode(message.planCode());
        notification.setPracticeCode(message.practiceCode());
        notification.setCorrelationId(correlationId);
        notification.setSentAt(OffsetDateTime.now(ZoneOffset.UTC));
        return notification;
    }

    private ProcessedMessage buildProcessedMessage(String messageId, String eventType, String correlationId) {
        ProcessedMessage processedMessage = new ProcessedMessage();
        processedMessage.setMessageId(messageId);
        processedMessage.setEventType(eventType);
        processedMessage.setCorrelationId(correlationId);
        return processedMessage;
    }
}
