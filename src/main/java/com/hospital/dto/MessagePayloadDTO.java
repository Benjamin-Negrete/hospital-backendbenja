package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayloadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    private String content;

    private String recipient; // Ej: email o teléfono del paciente

    private String category;  // NOTIFICATION, TICKET, DOCUMENT

    private boolean forceError; // Permite forzar error para demostrar la DLQ en la presentación

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
}
