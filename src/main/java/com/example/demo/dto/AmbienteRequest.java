package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un ambiente.
 */
public record AmbienteRequest(
        @NotNull Integer pisoId,
        @NotBlank @Size(max = 40) String tipo,
        @Size(max = 100) String especialidad,
        @Size(max = 20) String codigo,
        @NotBlank @Size(max = 120) String nombre
) {
}
