package com.example.demo.dto;

/**
 * Solicitud de recuperación de contraseña (olvidé mi contraseña).
 */
public record ForgotPasswordRequest(String correo) {
}
