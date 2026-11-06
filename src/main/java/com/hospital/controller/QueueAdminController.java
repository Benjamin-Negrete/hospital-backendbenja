package com.hospital.controller;

import com.hospital.dto.ApiResponseDTO;
import com.hospital.dto.CreateQueueDTO;
import com.hospital.service.RabbitAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

@RestController
@RequestMapping("/api/v1/admin/queues")
@Tag(name = "Administrador de RabbitMQ", description = "Endpoints REST para gestionar colas en RabbitMQ")
public class QueueAdminController {

    private final RabbitAdminService rabbitAdminService;

    public QueueAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva cola en RabbitMQ", description = "Valida que el nombre de la cola no esté vacío ni contenga caracteres inválidos")
    public ResponseEntity<ApiResponseDTO<String>> createQueue(@Valid @RequestBody CreateQueueDTO dto) {
        rabbitAdminService.createQueue(dto);
        ApiResponseDTO<String> response = new ApiResponseDTO<>(
                "SUCCESS",
                "Cola '" + dto.getName() + "' creada exitosamente en RabbitMQ",
                dto.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Eliminar una cola existente en RabbitMQ")
    public ResponseEntity<ApiResponseDTO<String>> deleteQueue(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO<>("ERROR", "El nombre de la cola es obligatorio", null));
        }

        boolean deleted = rabbitAdminService.deleteQueue(name);
        if (deleted) {
            ApiResponseDTO<String> response = new ApiResponseDTO<>(
                    "SUCCESS",
                    "Cola '" + name + "' eliminada exitosamente",
                    name
            );
            return ResponseEntity.ok(response);
        } else {
            ApiResponseDTO<String> response = new ApiResponseDTO<>(
                    "NOT_FOUND",
                    "La cola '" + name + "' no existe o no pudo ser eliminada",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/{name}/purge")
    @Operation(summary = "Purgar todos los mensajes de una cola")
    public ResponseEntity<ApiResponseDTO<String>> purgeQueue(@PathVariable String name) {
        rabbitAdminService.purgeQueue(name);
        ApiResponseDTO<String> response = new ApiResponseDTO<>(
                "SUCCESS",
                "Se purgaron todos los mensajes de la cola '" + name + "'",
                name
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    @Operation(summary = "Consultar estado y propiedades de una cola")
    public ResponseEntity<ApiResponseDTO<Properties>> getQueueInfo(@PathVariable String name) {
        Properties props = rabbitAdminService.getQueueProperties(name);
        if (props == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponseDTO<>("NOT_FOUND", "La cola '" + name + "' no existe", null)
            );
        }
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Propiedades de la cola obtenidas", props));
    }
}
