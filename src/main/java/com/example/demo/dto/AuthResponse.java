package com.example.demo.dto;

/**
 * DTO de respuesta de autenticación exitosa.
 */
public record AuthResponse(String token, String tipo, Long usuarioId, String correo, String rol) {
}
