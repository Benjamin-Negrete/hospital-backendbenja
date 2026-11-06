package com.hospital.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Centralización de nombres desde application.properties
    @Value("${app.rabbitmq.exchange.direct}")
    private String directExchangeName;

    @Value("${app.rabbitmq.exchange.topic}")
    private String topicExchangeName;

    @Value("${app.rabbitmq.exchange.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.queue.notifications}")
    private String notificationsQueueName;

    @Value("${app.rabbitmq.queue.notifications-dlq}")
    private String notificationsDlqName;

    @Value("${app.rabbitmq.queue.tickets}")
    private String ticketsQueueName;

    @Value("${app.rabbitmq.queue.tickets-dlq}")
    private String ticketsDlqName;

    @Value("${app.rabbitmq.queue.documents}")
    private String documentsQueueName;

    @Value("${app.rabbitmq.queue.documents-dlq}")
    private String documentsDlqName;

    @Value("${app.rabbitmq.routingkey.notifications}")
    private String notificationsRoutingKey;

    @Value("${app.rabbitmq.routingkey.tickets}")
    private String ticketsRoutingKey;

    @Value("${app.rabbitmq.routingkey.documents}")
    private String documentsRoutingKey;

    @Value("${app.rabbitmq.routingkey.dlq}")
    private String dlqRoutingKey;

    // ==========================================
    // 1. BEANS DE EXCHANGES (Direct, Topic, DLX)
    // ==========================================
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchangeName, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // ==========================================
    // 2. BEANS DE COLAS Y DLQS (3 Casos de uso)
    // ==========================================

    // Caso 1: Notificaciones de Pacientes
    @Bean
    public Queue notificationsDlq() {
        return QueueBuilder.durable(notificationsDlqName).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(notificationsQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey + ".notificaciones")
                .build();
    }

    // Caso 2: Generación de Tickets de Atención
    @Bean
    public Queue ticketsDlq() {
        return QueueBuilder.durable(ticketsDlqName).build();
    }

    @Bean
    public Queue ticketsQueue() {
        return QueueBuilder.durable(ticketsQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey + ".tickets")
                .build();
    }

    // Caso 3: Generación de Documentos / Fichas Médicas
    @Bean
    public Queue documentsDlq() {
        return QueueBuilder.durable(documentsDlqName).build();
    }

    @Bean
    public Queue documentsQueue() {
        return QueueBuilder.durable(documentsQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey + ".documentos")
                .build();
    }

    // ==========================================
    // 3. BEANS DE BINDINGS
    // ==========================================

    // Bindings de Colas Principales
    @Bean
    public Binding bindingNotifications(Queue notificationsQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(notificationsQueue).to(directExchange).with(notificationsRoutingKey);
    }

    @Bean
    public Binding bindingTickets(Queue ticketsQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(ticketsQueue).to(topicExchange).with(ticketsRoutingKey);
    }

    @Bean
    public Binding bindingDocuments(Queue documentsQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(documentsQueue).to(directExchange).with(documentsRoutingKey);
    }

    // Bindings de Dead Letter Queues
    @Bean
    public Binding bindingNotificationsDlq(Queue notificationsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationsDlq).to(deadLetterExchange).with(dlqRoutingKey + ".notificaciones");
    }

    @Bean
    public Binding bindingTicketsDlq(Queue ticketsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(ticketsDlq).to(deadLetterExchange).with(dlqRoutingKey + ".tickets");
    }

    @Bean
    public Binding bindingDocumentsDlq(Queue documentsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(documentsDlq).to(deadLetterExchange).with(dlqRoutingKey + ".documentos");
    }

    // ==========================================
    // 4. CONVERTIDOR JSON Y RABBITADMIN
    // ==========================================
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
