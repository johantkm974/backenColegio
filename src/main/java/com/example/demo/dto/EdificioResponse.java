package com.example.demo.dto;

/**
 * DTO de respuesta de edificio.
 */
public record EdificioResponse(
        Integer id,
        String codigo,
        String nombre,
        String descripcion,
        Boolean activo
) {
}
