package com.example.demo.service;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio para gestión de tokens de recuperación de contraseña.
 */
@Service
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UsuarioRepository usuarioRepository) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Genera un token de recuperación para el usuario con el correo dado.
     * Devuelve el token (en producción se enviaría por correo electrónico).
     */
    public String generarToken(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con correo: " + correo));

        // Invalidar tokens previos no usados
        tokenRepository.deleteByUsuarioId(usuario.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiracion = LocalDateTime.now().plusHours(1); // expira en 1 hora

        PasswordResetToken resetToken = new PasswordResetToken(usuario, token, expiracion);
        tokenRepository.save(resetToken);

        return token;
    }

    /**
     * Valida un token de recuperación y devuelve el usuario asociado si es válido.
     */
    public Usuario validarToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token no válido"));

        if (resetToken.getUsado()) {
            throw new IllegalArgumentException("El token ya fue utilizado");
        }

        if (resetToken.isExpirado()) {
            throw new IllegalArgumentException("El token ha expirado");
        }

        return resetToken.getUsuario();
    }

    /**
     * Marca el token como usado.
     */
    public void marcarComoUsado(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token no válido"));
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);
    }
}
