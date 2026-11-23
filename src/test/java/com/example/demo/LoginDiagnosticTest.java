package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de regresión del login: valida que AuthenticationManager autentique al
 * usuario ADMIN con su contraseña real y que el rol (entidad LAZY) se pueda
 * leer sin LazyInitializationException — que era la causa raíz del 403 en
 * POST /api/auth/login.
 */
@SpringBootTest
class LoginDiagnosticTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void loginAdminDeveAutenticarComRol() {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("admin@institucion.edu.pe", "admin123"));

        assertNotNull(auth, "La autenticación no debe ser nula");
        assertTrue(auth.isAuthenticated(), "El usuario debe quedar autenticado");

        boolean tieneRolAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        assertTrue(tieneRolAdmin, "Debe tener la autoridad ROLE_ADMIN, pero fue: " + auth.getAuthorities());
    }
}
