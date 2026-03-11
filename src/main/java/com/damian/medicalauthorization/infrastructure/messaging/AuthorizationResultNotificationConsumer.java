package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.shared.logging.CorrelationIdHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
public class AuthorizationResultNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationResultNotificationConsumer.class);

    private static final Set<String> SUPPORTED_RESULT_EVENTS = Set.of(
            "AuthorizationApproved",
            "AuthorizationRejected",
            "AuthorizationRequiresAudit"
    );

    private final ObjectMapper objectMapper;
    private final AuthorizationResultNotificationProcessingService processingService;

    public AuthorizationResultNotificationConsumer(
            ObjectMapper objectMapper,
            AuthorizationResultNotificationProcessingService processingService
    ) {
        this.objectMapper = objectMapper;
        this.processingService = processingService;
    }

    @RabbitListener(queues = "${app.messaging.rabbit.authorization-result-queue}")
    public void consume(
            String payload,
            @Header(name = AmqpHeaders.MESSAGE_ID, required = false) String messageId,
            @Header(name = "eventType", required = false) String eventType,
            @Header(name = AmqpHeaders.CORRELATION_ID, required = false) String correlationIdHeader
    ) {
        AuthorizationResultMessage message = deserialize(payload);
        String resolvedEventType = resolveEventType(eventType);
        String resolvedCorrelationId = resolveCorrelationId(message.correlationId(), correlationIdHeader);

        String resolvedMessageId = resolveMessageId(message, messageId);

        try {
            MDC.put(CorrelationIdHolder.CORRELATION_ID_MDC_KEY, resolvedCorrelationId);
            processingService.process(
                    message,
                    resolvedMessageId,
                    resolvedEventType,
                    resolvedCorrelationId
            );
        } catch (RuntimeException ex) {
            logger.warn(
                    "AuthorizationResult processing failed on attempt {}. correlationId={}, eventId={}",
                    currentRetryAttempt(),
                    resolvedCorrelationId,
                    resolvedMessageId,
                    ex
            );
            throw ex;
        } finally {
            MDC.remove(CorrelationIdHolder.CORRELATION_ID_MDC_KEY);
        }
    }

    private AuthorizationResultMessage deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, AuthorizationResultMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid authorization result payload", e);
        }
    }

    private String resolveMessageId(AuthorizationResultMessage message, String messageIdHeader) {
        if (messageIdHeader != null && !messageIdHeader.isBlank()) {
            return messageIdHeader;
        }

        UUID authorizationId = message.authorizationId();
        if (authorizationId == null) {
            throw new IllegalArgumentException("Authorization result payload is missing authorizationId");
        }

        return "AuthorizationResult-" + authorizationId + "-" + message.status();
    }

    private String resolveEventType(String eventTypeHeader) {
        if (eventTypeHeader == null || eventTypeHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization result event is missing eventType header");
        }

        if (!SUPPORTED_RESULT_EVENTS.contains(eventTypeHeader)) {
            throw new IllegalArgumentException("Unsupported authorization result eventType: " + eventTypeHeader);
        }

        return eventTypeHeader;
    }

    private int currentRetryAttempt() {
        if (RetrySynchronizationManager.getContext() == null) {
            return 1;
        }

        return RetrySynchronizationManager.getContext().getRetryCount() + 1;
    }

    private String resolveCorrelationId(String payloadCorrelationId, String headerCorrelationId) {
        if (payloadCorrelationId != null && !payloadCorrelationId.isBlank()) {
            return payloadCorrelationId;
        }

        if (headerCorrelationId != null && !headerCorrelationId.isBlank()) {
            return headerCorrelationId;
        }

        return CorrelationIdHolder.getOrDefault();
    }
}
