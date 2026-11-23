package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un reporte.
 */
public record ReporteRequest(
        @NotNull Integer ambienteId,
        @NotBlank @Size(max = 150) String titulo,
        @NotBlank String descripcion,
        String fotoUrl,
        @Size(max = 10) String prioridad,
        String estado
) {
}
