package com.hospital.consumer;

import com.hospital.dto.MessagePayloadDTO;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio Funcional: Auditoría y Recuperación de Fallos (Dead Letter Queues)
 * Registra formalmente los mensajes no entregados que llegaron a las DLQs.
 */
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.notifications-dlq}", ackMode = "MANUAL")
    public void processNotificationsDlq(
            MessagePayloadDTO message, 
            Channel channel, 
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        log.warn("🚨 [DLQ-AUDIT] Mensaje recibido en 'cola.notificaciones.dlq': ID={}, Contenido='{}'",
                message.getMessageId(), message.getContent());
        // Confirmar lectura de la DLQ para auditoría
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.tickets-dlq}", ackMode = "MANUAL")
    public void processTicketsDlq(
            MessagePayloadDTO message, 
            Channel channel, 
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        log.warn("🚨 [DLQ-AUDIT] Mensaje recibido en 'cola.tickets.dlq': ID={}, Contenido='{}'",
                message.getMessageId(), message.getContent());
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.documents-dlq}", ackMode = "MANUAL")
    public void processDocumentsDlq(
            MessagePayloadDTO message, 
            Channel channel, 
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        log.warn("🚨 [DLQ-AUDIT] Mensaje recibido en 'cola.documentos.dlq': ID={}, Contenido='{}'",
                message.getMessageId(), message.getContent());
        channel.basicAck(tag, false);
    }
}
