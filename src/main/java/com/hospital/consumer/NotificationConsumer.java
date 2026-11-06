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
 * Dominio Funcional: Notificaciones al Paciente
 * Implementa confirmación explícita (ACK/NACK) y desvío a DLQ ante fallos.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.notifications}", ackMode = "MANUAL")
    public void consumeNotification(
            MessagePayloadDTO message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info(">> [NOTIFICATION-CONSUMER] Recibido mensaje ID: {} - Destinatario: {}", 
                message.getMessageId(), message.getRecipient());

        try {
            // Validación de contenido o simulación de error para prueba de DLQ
            if (message.isForceError() || (message.getContent() != null && message.getContent().toUpperCase().contains("ERROR"))) {
                throw new IllegalStateException("Error simulado o contenido inválido detectado en la notificación");
            }

            // Procesamiento de negocio (Ej: envío de correo / SMS)
            log.info(">> [NOTIFICATION-CONSUMER] Notificación procesada exitosamente: '{}'", message.getContent());

            // 1. Confirmación positiva explícita (ACK)
            channel.basicAck(deliveryTag, false);
            log.info(">> [NOTIFICATION-CONSUMER] ACK enviado para deliveryTag: {}", deliveryTag);

        } catch (Exception ex) {
            log.error(">> [NOTIFICATION-CONSUMER] Error al procesar notificación ID {}: {}", 
                    message.getMessageId(), ex.getMessage());

            // 2. Manejo de error y desvío a DLQ:
            // basicNack con requeue = false -> RabbitMQ traslada el mensaje al Dead Letter Exchange configurado
            channel.basicNack(deliveryTag, false, false);
            log.warn(">> [NOTIFICATION-CONSUMER] NACK (requeue=false) enviado. Mensaje enviado a DLQ: cola.notificaciones.dlq");
        }
    }
}
