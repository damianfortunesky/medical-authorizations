package com.damian.medicalauthorization.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);
    private static final String DEAD_LETTER_EXCHANGE_SUFFIX = ".dlx";
    private static final String DEAD_LETTER_QUEUE_SUFFIX = ".dlq";
    private static final String DEAD_LETTER_ROUTING_KEY_SUFFIX = ".dlq";
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1_000L;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long MAX_BACKOFF_MS = 8_000L;

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    DirectExchange authorizationExchange(RabbitProperties rabbitProperties) {
        return new DirectExchange(rabbitProperties.exchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    DirectExchange authorizationDeadLetterExchange(RabbitProperties rabbitProperties) {
        return new DirectExchange(rabbitProperties.exchange() + DEAD_LETTER_EXCHANGE_SUFFIX, true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue authorizationCreatedQueue(RabbitProperties rabbitProperties) {
        return buildMainQueueWithDlq(
                rabbitProperties.authorizationCreatedQueue(),
                rabbitProperties.exchange() + DEAD_LETTER_EXCHANGE_SUFFIX,
                rabbitProperties.authorizationCreatedRoutingKey() + DEAD_LETTER_ROUTING_KEY_SUFFIX
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding authorizationCreatedBinding(
            @Qualifier("authorizationCreatedQueue") Queue authorizationCreatedQueue,
            @Qualifier("authorizationExchange") DirectExchange authorizationExchange,
            RabbitProperties rabbitProperties
    ) {
        return BindingBuilder
                .bind(authorizationCreatedQueue)
                .to(authorizationExchange)
                .with(rabbitProperties.authorizationCreatedRoutingKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue authorizationResultQueue(RabbitProperties rabbitProperties) {
        return buildMainQueueWithDlq(
                rabbitProperties.authorizationResultQueue(),
                rabbitProperties.exchange() + DEAD_LETTER_EXCHANGE_SUFFIX,
                rabbitProperties.authorizationResultRoutingKey() + DEAD_LETTER_ROUTING_KEY_SUFFIX
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding authorizationResultBinding(
            @Qualifier("authorizationResultQueue") Queue authorizationResultQueue,
            @Qualifier("authorizationExchange") DirectExchange authorizationExchange,
            RabbitProperties rabbitProperties
    ) {
        return BindingBuilder
                .bind(authorizationResultQueue)
                .to(authorizationExchange)
                .with(rabbitProperties.authorizationResultRoutingKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue authorizationCreatedDeadLetterQueue(RabbitProperties rabbitProperties) {
        return new Queue(rabbitProperties.authorizationCreatedQueue() + DEAD_LETTER_QUEUE_SUFFIX, true);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding authorizationCreatedDeadLetterBinding(
            @Qualifier("authorizationCreatedDeadLetterQueue") Queue authorizationCreatedDeadLetterQueue,
            @Qualifier("authorizationDeadLetterExchange") DirectExchange authorizationDeadLetterExchange,
            RabbitProperties rabbitProperties
    ) {
        return BindingBuilder
                .bind(authorizationCreatedDeadLetterQueue)
                .to(authorizationDeadLetterExchange)
                .with(rabbitProperties.authorizationCreatedRoutingKey() + DEAD_LETTER_ROUTING_KEY_SUFFIX);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue authorizationResultDeadLetterQueue(RabbitProperties rabbitProperties) {
        return new Queue(rabbitProperties.authorizationResultQueue() + DEAD_LETTER_QUEUE_SUFFIX, true);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding authorizationResultDeadLetterBinding(
            @Qualifier("authorizationResultDeadLetterQueue") Queue authorizationResultDeadLetterQueue,
            @Qualifier("authorizationDeadLetterExchange") DirectExchange authorizationDeadLetterExchange,
            RabbitProperties rabbitProperties
    ) {
        return BindingBuilder
                .bind(authorizationResultDeadLetterQueue)
                .to(authorizationDeadLetterExchange)
                .with(rabbitProperties.authorizationResultRoutingKey() + DEAD_LETTER_ROUTING_KEY_SUFFIX);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    RetryOperationsInterceptor rabbitRetryInterceptor() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(IllegalArgumentException.class, false);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(MAX_RETRIES, retryableExceptions, true);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_BACKOFF_MS);
        backOffPolicy.setMultiplier(BACKOFF_MULTIPLIER);
        backOffPolicy.setMaxInterval(MAX_BACKOFF_MS);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.registerListener(new RetryAttemptLoggingListener());

        return RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .recoverer(deadLetterRejectRecoverer())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    MessageRecoverer deadLetterRejectRecoverer() {
        return (message, cause) -> {
            logger.error(
                    "Retries exhausted. Moving message to DLQ. correlationId={}, eventId={}",
                    headerValue(message, AmqpHeaders.CORRELATION_ID),
                    resolveEventId(message),
                    cause
            );
            new RejectAndDontRequeueRecoverer().recover(message, cause);
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor rabbitRetryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(rabbitRetryInterceptor);
        return factory;
    }

    private Queue buildMainQueueWithDlq(String queueName, String deadLetterExchange, String deadLetterRoutingKey) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(deadLetterExchange)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    private static String resolveEventId(Message message) {
        String messageId = headerValue(message, AmqpHeaders.MESSAGE_ID);
        if (messageId != null && !messageId.isBlank()) {
            return messageId;
        }

        String eventId = headerValue(message, "eventId");
        if (eventId != null && !eventId.isBlank()) {
            return eventId;
        }

        return "unknown";
    }

    private static String headerValue(Message message, String headerName) {
        Object value = message.getMessageProperties().getHeaders().get(headerName);
        if (value != null) {
            return value.toString();
        }

        if (AmqpHeaders.MESSAGE_ID.equals(headerName)) {
            return message.getMessageProperties().getMessageId();
        }

        if (AmqpHeaders.CORRELATION_ID.equals(headerName)) {
            return message.getMessageProperties().getCorrelationId();
        }

        return null;
    }

    private static class RetryAttemptLoggingListener implements RetryListener {

        @Override
        public <T, E extends Throwable> void onError(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable
        ) {
            Message message = extractFailedMessage(throwable);
            if (message == null) {
                logger.warn("Retry attempt {} failed for message with unknown metadata", context.getRetryCount(), throwable);
                return;
            }

            logger.warn(
                    "Retry attempt {} failed. correlationId={}, eventId={}",
                    context.getRetryCount(),
                    headerValue(message, AmqpHeaders.CORRELATION_ID),
                    resolveEventId(message),
                    throwable
            );
        }

        private Message extractFailedMessage(Throwable throwable) {
            if (throwable instanceof ListenerExecutionFailedException listenerException) {
                return listenerException.getFailedMessage();
            }

            return null;
        }
    }
}
