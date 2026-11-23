package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un usuario.
 */
public record UsuarioRequest(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank @Size(max = 15) String dni,
        @NotBlank @Email @Size(max = 120) String correo,
        String password,
        @Size(max = 20) String telefono,
        Integer rolId
) {
}
