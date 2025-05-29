package com.payiskoul.institution.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration du message broker pour le service Institutions
 */
@Configuration
@Profile({"default","dev","local"})
public class RabbitMQConfig {

    // Exchanges
    public static final String TRANSACTION_EXCHANGE = "payiskoul.transaction.exchange";
    public static final String TUITION_EXCHANGE = "payiskoul.tuition.exchange";
    public static final String PAYISKOUL_EVENTS_EXCHANGE = "payiskoul.events";

    // Routing keys
    public static final String TRANSACTION_ROUTING_KEY = "transaction.event";
    public static final String TUITION_PAYMENT_CONFIRMED_ROUTING_KEY = "tuition.payment.confirmed";
    public static final String TUITION_PAYMENT_FAILED_ROUTING_KEY = "tuition.payment.failed";

    // Queues
    public static final String TRANSACTION_QUEUE = "payiskoul.transaction.queue";
    public static final String TUITION_PAYMENT_QUEUE = "payiskoul.tuitions.queue";

    /**
     * Configurateur de convertisseur de messages JSON
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configuration du template RabbitMQ
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // --- Configuration des Exchanges ---

    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public DirectExchange tuitionExchange() {
        return new DirectExchange(TUITION_EXCHANGE);
    }

    @Bean
    public TopicExchange payiskoulEventsExchange() {
        return new TopicExchange(PAYISKOUL_EVENTS_EXCHANGE);
    }

    // --- Configuration des Queues ---

    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(TRANSACTION_QUEUE).build();
    }

    @Bean
    public Queue tuitionPaymentQueue() {
        return QueueBuilder.durable(TUITION_PAYMENT_QUEUE).build();
    }

    // --- Configuration des Bindings ---

    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(transactionExchange())
                .with(TRANSACTION_ROUTING_KEY);
    }

    @Bean
    public Binding tuitionPaymentBinding() {
        return BindingBuilder
                .bind(tuitionPaymentQueue())
                .to(payiskoulEventsExchange())
                .with("tuition.payment.#");
    }
}