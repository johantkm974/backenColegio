package com.example.demo.dto;

/**
 * DTO de respuesta de piso.
 */
public record PisoResponse(
        Integer id,
        Integer edificioId,
        String edificioCodigo,
        Integer numeroPiso,
        String nombre,
        Boolean activo
) {
}
