package com.example.demo.dto;

/**
 * DTO de respuesta de usuario (sin exponer password).
 */
public record UsuarioResponse(
        Long id,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String dni,
        String correo,
        String telefono,
        Boolean activo,
        Integer rolId,
        String rolNombre
) {
}
