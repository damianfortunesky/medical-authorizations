package com.damian.medicalauthorization.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    DirectExchange authorizationExchange(RabbitProperties rabbitProperties) {
        return new DirectExchange(rabbitProperties.exchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue authorizationCreatedQueue(RabbitProperties rabbitProperties) {
        return new Queue(rabbitProperties.authorizationCreatedQueue(), true);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding authorizationCreatedBinding(
            Queue authorizationCreatedQueue,
            DirectExchange authorizationExchange,
            RabbitProperties rabbitProperties
    ) {
        return BindingBuilder
                .bind(authorizationCreatedQueue)
                .to(authorizationExchange)
                .with(rabbitProperties.authorizationCreatedRoutingKey());
    }
}
