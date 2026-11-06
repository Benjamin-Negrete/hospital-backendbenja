package com.hospital.service;

import com.hospital.dto.MessagePayloadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HospitalMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(HospitalMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.direct}")
    private String directExchange;

    @Value("${app.rabbitmq.exchange.topic}")
    private String topicExchange;

    @Value("${app.rabbitmq.routingkey.notifications}")
    private String notificationsKey;

    @Value("${app.rabbitmq.routingkey.documents}")
    private String documentsKey;

    public HospitalMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Envía un mensaje a la cola de notificaciones vía Direct Exchange.
     */
    public void sendNotification(MessagePayloadDTO message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        message.setCategory("NOTIFICATION");
        log.info("Publicando notificación [ID: {}] en Exchange [{}] con Key [{}]", 
                message.getMessageId(), directExchange, notificationsKey);
        rabbitTemplate.convertAndSend(directExchange, notificationsKey, message);
    }

    /**
     * Envía un ticket de atención vía Topic Exchange.
     * Ejemplo de routing key: hospital.ticket.urgencias, hospital.ticket.pediatria
     */
    public void sendTicket(String specialty, MessagePayloadDTO message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        message.setCategory("TICKET");
        String routingKey = "hospital.ticket." + specialty.toLowerCase().trim();
        log.info("Publicando ticket [ID: {}] en Topic Exchange [{}] con Key [{}]", 
                message.getMessageId(), topicExchange, routingKey);
        rabbitTemplate.convertAndSend(topicExchange, routingKey, message);
    }

    /**
     * Envía solicitud de generación de documentos vía Direct Exchange.
     */
    public void sendDocument(MessagePayloadDTO message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        message.setCategory("DOCUMENT");
        log.info("Publicando documento [ID: {}] en Exchange [{}] con Key [{}]", 
                message.getMessageId(), directExchange, documentsKey);
        rabbitTemplate.convertAndSend(directExchange, documentsKey, message);
    }
}
