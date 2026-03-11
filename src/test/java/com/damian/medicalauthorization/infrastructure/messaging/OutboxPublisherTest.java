package com.damian.medicalauthorization.infrastructure.messaging;

import com.damian.medicalauthorization.infrastructure.config.RabbitProperties;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEvent;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEventStatus;
import com.damian.medicalauthorization.infrastructure.persistence.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitProperties rabbitProperties;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    void shouldPublishPendingEventAndMarkAsPublished() {
        OutboxEvent event = buildPendingEvent();

        when(rabbitProperties.enabled()).thenReturn(true);
        when(rabbitProperties.exchange()).thenReturn("medical.authorization.exchange");
        when(rabbitProperties.authorizationCreatedRoutingKey()).thenReturn("authorization.created");
        when(outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(event));

        outboxPublisher.publishPendingEvents();

        ArgumentCaptor<OutboxEvent> outboxEventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxEventCaptor.capture());

        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals(OutboxEventStatus.PUBLISHED, savedEvent.getStatus());
        assertNotNull(savedEvent.getPublishedAt());
        assertEquals(0, savedEvent.getRetryCount());

        verify(rabbitTemplate).convertAndSend(eq("medical.authorization.exchange"), eq("authorization.created"), eq(event.getPayloadJson()), any());
    }

    @Test
    void shouldIncrementRetryCountWhenPublishingFails() {
        OutboxEvent event = buildPendingEvent();

        when(rabbitProperties.enabled()).thenReturn(true);
        when(rabbitProperties.exchange()).thenReturn("medical.authorization.exchange");
        when(rabbitProperties.authorizationCreatedRoutingKey()).thenReturn("authorization.created");
        when(outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(event));
        doThrow(new RuntimeException("broker unavailable"))
                .when(rabbitTemplate)
                .convertAndSend(eq("medical.authorization.exchange"), eq("authorization.created"), eq(event.getPayloadJson()), any());

        outboxPublisher.publishPendingEvents();

        ArgumentCaptor<OutboxEvent> outboxEventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxEventCaptor.capture());

        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals(OutboxEventStatus.PENDING, savedEvent.getStatus());
        assertEquals(1, savedEvent.getRetryCount());
    }

    @Test
    void shouldSkipPublishingWhenRabbitIsDisabled() {
        when(rabbitProperties.enabled()).thenReturn(false);

        outboxPublisher.publishPendingEvents();

        verify(outboxEventRepository, never()).findTop100ByStatusOrderByCreatedAtAsc(any());
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(), any());
    }

    private OutboxEvent buildPendingEvent() {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setAggregateId(UUID.randomUUID());
        event.setAggregateType("Authorization");
        event.setEventType("AuthorizationCreated");
        event.setPayloadJson("{\"authorizationId\":\"abc\"}");
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setCorrelationId("corr-123");
        event.setCreatedAt(OffsetDateTime.now());
        return event;
    }
}
