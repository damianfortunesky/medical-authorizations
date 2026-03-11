package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.shared.logging.CorrelationIdHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
public class AuthorizationCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final AuthorizationCreatedProcessingService processingService;

    public AuthorizationCreatedConsumer(
            ObjectMapper objectMapper,
            AuthorizationCreatedProcessingService processingService
    ) {
        this.objectMapper = objectMapper;
        this.processingService = processingService;
    }

    @RabbitListener(queues = "${app.messaging.rabbit.authorization-created-queue}")
    public void consume(
            String payload,
            @Header(name = AmqpHeaders.MESSAGE_ID, required = false) String messageId,
            @Header(name = AmqpHeaders.CORRELATION_ID, required = false) String correlationIdHeader
    ) {
        AuthorizationCreatedMessage message = deserialize(payload);
        String resolvedCorrelationId = resolveCorrelationId(message.correlationId(), correlationIdHeader);

        try {
            MDC.put(CorrelationIdHolder.CORRELATION_ID_MDC_KEY, resolvedCorrelationId);
            processingService.process(message, resolveMessageId(message, messageId), resolvedCorrelationId);
        } finally {
            MDC.remove(CorrelationIdHolder.CORRELATION_ID_MDC_KEY);
        }
    }

    private AuthorizationCreatedMessage deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, AuthorizationCreatedMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid AuthorizationCreated payload", e);
        }
    }

    private String resolveMessageId(AuthorizationCreatedMessage message, String messageIdHeader) {
        if (messageIdHeader != null && !messageIdHeader.isBlank()) {
            return messageIdHeader;
        }

        UUID authorizationId = message.authorizationId();
        if (authorizationId == null) {
            throw new IllegalArgumentException("AuthorizationCreated payload is missing authorizationId");
        }

        return "AuthorizationCreated-" + authorizationId;
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
