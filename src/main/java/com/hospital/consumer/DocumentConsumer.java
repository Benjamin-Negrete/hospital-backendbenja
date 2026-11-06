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
 * Dominio Funcional: Generación de Documentos y Recetas Clínicas
 * Implementa confirmación explícita (ACK/NACK) y DLQ.
 */
@Component
public class DocumentConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.documents}", ackMode = "MANUAL")
    public void consumeDocument(
            MessagePayloadDTO message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info(">> [DOCUMENT-CONSUMER] Recibida solicitud de documento ID: {}", message.getMessageId());

        try {
            if (message.isForceError() || (message.getContent() != null && message.getContent().toUpperCase().contains("ERROR"))) {
                throw new RuntimeException("Error en motor de renderizado de documentos clínicos");
            }

            // Simular generación de PDF / receta
            log.info(">> [DOCUMENT-CONSUMER] Documento generado exitosamente para destinatario: {}", message.getRecipient());

            // ACK explícito
            channel.basicAck(deliveryTag, false);
            log.info(">> [DOCUMENT-CONSUMER] ACK enviado para deliveryTag: {}", deliveryTag);

        } catch (Exception ex) {
            log.error(">> [DOCUMENT-CONSUMER] Error al generar documento ID {}: {}", 
                    message.getMessageId(), ex.getMessage());

            // NACK y envío a DLQ
            channel.basicNack(deliveryTag, false, false);
            log.warn(">> [DOCUMENT-CONSUMER] NACK enviado. Movido a cola.documentos.dlq");
        }
    }
}
