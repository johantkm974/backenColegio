package com.example.demo.dto;

/**
 * DTO de solicitud de login.
 */
public record LoginRequest(String correo, String password) {
}
