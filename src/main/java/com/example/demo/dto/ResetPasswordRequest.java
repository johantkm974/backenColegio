package com.example.demo.dto;

/**
 * Solicitud de restablecimiento de contraseña.
 */
public record ResetPasswordRequest(String token, String nuevaPassword) {
}
