package com.example.demo.dto;

/**
 * DTO de respuesta de ambiente.
 */
public record AmbienteResponse(
        Integer id,
        Integer pisoId,
        String tipo,
        String especialidad,
        String codigo,
        String nombre,
        Boolean activo
) {
}
