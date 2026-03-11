package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.infrastructure.config.RabbitProperties;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEvent;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEventStatus;
import com.damian.medicalauthorization.infrastructure.persistence.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties rabbitProperties;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            RabbitTemplate rabbitTemplate,
            RabbitProperties rabbitProperties
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;
    }

    @Scheduled(fixedDelayString = "${app.messaging.outbox.publisher-fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        if (!rabbitProperties.enabled()) {
            return;
        }

        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        logger.info("Publishing {} outbox event(s)", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            publishSingleEvent(event);
        }
    }

    private void publishSingleEvent(OutboxEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    rabbitProperties.exchange(),
                    rabbitProperties.authorizationCreatedRoutingKey(),
                    event.getPayloadJson(),
                    message -> {
                        MessageProperties properties = message.getMessageProperties();
                        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        properties.setMessageId(event.getId().toString());
                        properties.setHeader("eventType", event.getEventType());
                        properties.setHeader("aggregateType", event.getAggregateType());
                        properties.setHeader("aggregateId", event.getAggregateId().toString());
                        properties.setHeader("correlationId", event.getCorrelationId());
                        return message;
                    }
            );

            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(OffsetDateTime.now(ZoneOffset.UTC));
            outboxEventRepository.save(event);

            logger.debug("Outbox event {} published successfully", event.getId());
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            outboxEventRepository.save(event);

            logger.warn("Failed to publish outbox event {}. retryCount={}", event.getId(), event.getRetryCount(), ex);
        }
    }
}
