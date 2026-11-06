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
 * Dominio Funcional: Tickets de Atención Médica
 * Escucha la cola de tickets asociada al Topic Exchange con ACK manual y DLQ.
 */
@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.tickets}", ackMode = "MANUAL")
    public void consumeTicket(
            MessagePayloadDTO message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info(">> [TICKET-CONSUMER] Recibido Ticket ID: {} - Asunto: {}", 
                message.getMessageId(), message.getContent());

        try {
            if (message.isForceError() || (message.getContent() != null && message.getContent().toUpperCase().contains("ERROR"))) {
                throw new IllegalArgumentException("Fallo en la emisión del ticket médico");
            }

            // Procesamiento de asignación de turno
            log.info(">> [TICKET-CONSUMER] Turno médico generado correctamente: {}", message.getContent());

            // ACK explícito
            channel.basicAck(deliveryTag, false);
            log.info(">> [TICKET-CONSUMER] ACK enviado para deliveryTag: {}", deliveryTag);

        } catch (Exception ex) {
            log.error(">> [TICKET-CONSUMER] Error en Ticket ID {}: {}. Derivando a DLQ...", 
                    message.getMessageId(), ex.getMessage());

            // NACK sin requeue -> a la DLQ de tickets
            channel.basicNack(deliveryTag, false, false);
            log.warn(">> [TICKET-CONSUMER] NACK enviado. Derivado a cola.tickets.dlq");
        }
    }
}
