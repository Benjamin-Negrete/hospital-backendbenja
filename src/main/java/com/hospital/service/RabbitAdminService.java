package com.hospital.service;

import com.hospital.dto.CreateQueueDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    /**
     * Crea una nueva cola en RabbitMQ encapsulando la lógica interna.
     */
    public boolean createQueue(CreateQueueDTO dto) {
        try {
            Queue queue = new Queue(dto.getName(), dto.isDurable(), dto.isExclusive(), dto.isAutoDelete());
            String declaredQueue = rabbitAdmin.declareQueue(queue);
            log.info("Cola declarada exitosamente en RabbitMQ: {}", declaredQueue);
            return declaredQueue != null;
        } catch (Exception e) {
            log.error("Error al crear la cola en RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la cola en RabbitMQ: " + e.getMessage());
        }
    }

    /**
     * Elimina una cola por nombre.
     */
    public boolean deleteQueue(String queueName) {
        try {
            boolean result = rabbitAdmin.deleteQueue(queueName);
            if (result) {
                log.info("Cola eliminada exitosamente: {}", queueName);
            } else {
                log.warn("La cola {} no existía o no pudo ser eliminada.", queueName);
            }
            return result;
        } catch (Exception e) {
            log.error("Error al eliminar la cola {}: {}", queueName, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar la cola: " + e.getMessage());
        }
    }

    /**
     * Purga todos los mensajes pendientes de una cola.
     */
    public boolean purgeQueue(String queueName) {
        try {
            rabbitAdmin.purgeQueue(queueName, false);
            log.info("Se purgaron los mensajes de la cola {}", queueName);
            return true;
        } catch (Exception e) {
            log.error("Error al purgar la cola {}: {}", queueName, e.getMessage(), e);
            throw new RuntimeException("Error al purgar la cola: " + e.getMessage());
        }
    }

    /**
     * Consulta propiedades/estado de una cola en RabbitMQ.
     */
    public Properties getQueueProperties(String queueName) {
        return rabbitAdmin.getQueueProperties(queueName);
    }
}
