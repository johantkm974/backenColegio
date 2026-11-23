package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un piso.
 */
public record PisoRequest(
        @NotNull Integer edificioId,
        @NotNull Integer numeroPiso,
        @NotBlank @Size(max = 50) String nombre
) {
}
