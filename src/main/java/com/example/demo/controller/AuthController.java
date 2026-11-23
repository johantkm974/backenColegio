package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          PasswordResetService passwordResetService,
                          UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.correo(), request.password())
            );
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Tu cuenta está desactivada. Contacta al administrador."));
        } catch (BadCredentialsException e) {
            boolean correoExiste = usuarioService.buscarPorCorreo(request.correo()).isPresent();
            String msg = correoExiste
                    ? "La contraseña no es correcta."
                    : "No existe una cuenta registrada con ese correo.";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", msg));
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.correo());
        final String jwt = jwtUtil.generateToken(userDetails);

        var usuario = ((com.example.demo.security.CustomUserDetails) userDetails).getUsuario();

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                "Bearer",
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getRol().getNombre()
        ));
    }

    /**
     * Olvidé mi contraseña: genera un token de recuperación.
     * En producción este token se enviaría por correo electrónico.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String token = passwordResetService.generarToken(request.correo());
        return ResponseEntity.ok(token);
    }

    /**
     * Restablece la contraseña usando un token válido.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        var usuario = passwordResetService.validarToken(request.token());
        usuarioService.cambiarContrasena(usuario.getId(), request.nuevaPassword());
        passwordResetService.marcarComoUsado(request.token());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> me() {
        return ResponseEntity.ok("Endpoint protegido funcionando");
    }
}
