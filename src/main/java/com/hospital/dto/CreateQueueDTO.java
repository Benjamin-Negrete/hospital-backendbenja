package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQueueDTO {

    @NotBlank(message = "El nombre de la cola no puede estar vacío ni contener solo espacios")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre de la cola solo puede contener letras, números, puntos (.), guiones (-) o guiones bajos (_)"
    )
    private String name;

    private boolean durable = true;

    private boolean autoDelete = false;

    private boolean exclusive = false;
}
