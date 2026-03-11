package com.damian.medicalauthorization.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigurationTest {

    private final RabbitConfiguration rabbitConfiguration = new RabbitConfiguration();

    @Test
    void shouldConfigureMainQueuesWithDeadLetterRouting() {
        RabbitProperties rabbitProperties = new RabbitProperties(
                true,
                "medical.authorization.exchange",
                "authorization.created",
                "authorization.created.processing",
                "authorization.result",
                "authorization.result.notifications"
        );

        Queue authorizationCreatedQueue = rabbitConfiguration.authorizationCreatedQueue(rabbitProperties);
        Queue authorizationResultQueue = rabbitConfiguration.authorizationResultQueue(rabbitProperties);

        assertThat(authorizationCreatedQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", "medical.authorization.exchange.dlx")
                .containsEntry("x-dead-letter-routing-key", "authorization.created.dlq");

        assertThat(authorizationResultQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", "medical.authorization.exchange.dlx")
                .containsEntry("x-dead-letter-routing-key", "authorization.result.dlq");
    }
}
