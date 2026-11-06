package com.hospital.controller;

import com.hospital.dto.ApiResponseDTO;
import com.hospital.dto.MessagePayloadDTO;
import com.hospital.service.HospitalMessageProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Pruebas de Mensajería RabbitMQ", description = "Endpoints para enviar mensajes y probar ACK, NACK y DLQ")
public class MessageTestController {

    private final HospitalMessageProducer producer;

    public MessageTestController(HospitalMessageProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/notification")
    @Operation(summary = "Enviar notificación (Direct Exchange -> cola.notificaciones)", 
               description = "Si marcas 'forceError: true' o colocas 'ERROR' en el contenido, el consumidor enviará NACK y el mensaje caerá en cola.notificaciones.dlq")
    public ResponseEntity<ApiResponseDTO<MessagePayloadDTO>> sendNotification(@Valid @RequestBody MessagePayloadDTO dto) {
        producer.sendNotification(dto);
        String statusMessage = dto.isForceError() 
                ? "Notificación enviada con error forzado. Revisa la consola para ver el NACK y la DLQ!" 
                : "Notificación enviada exitosamente. Revisa la consola para ver el ACK!";
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", statusMessage, dto));
    }

    @PostMapping("/ticket/{specialty}")
    @Operation(summary = "Emitir Ticket (Topic Exchange -> cola.tickets)", 
               description = "Usa routing keys dinámicas como hospital.ticket.pediatria o hospital.ticket.urgencias")
    public ResponseEntity<ApiResponseDTO<MessagePayloadDTO>> sendTicket(
            @PathVariable String specialty, 
            @Valid @RequestBody MessagePayloadDTO dto
    ) {
        producer.sendTicket(specialty, dto);
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Ticket para especialidad '" + specialty + "' emitido", dto));
    }

    @PostMapping("/document")
    @Operation(summary = "Generar Documento Clínico (Direct Exchange -> cola.documentos)")
    public ResponseEntity<ApiResponseDTO<MessagePayloadDTO>> sendDocument(@Valid @RequestBody MessagePayloadDTO dto) {
        producer.sendDocument(dto);
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Solicitud de documento clínico enviada", dto));
    }
}
