package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de regresión de las reglas de autorización de {@code SecurityConfig}.
 *
 * <p>Responde a la duda de si {@code /api/edificios/**} matchea también la ruta
 * base {@code /api/edificios} (Spring Security 7 usa PathPattern). Se comprueba
 * empíricamente con MockMvc, sin escribir en la BD: se usa {@code PUT} sobre un
 * id inexistente, que devuelve 404 cuando la autorización pasa y 403 cuando
 * la regla de rol bloquea.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadInfraestructuraTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String JSON_EDIFICIO =
            "{\"codigo\":\"ED-99\",\"nombre\":\"No Guarda\"}";

    // ---------- HEALTH CHECK PUBLICO (sin JWT) ----------

    /**
     * El cron job de Render hace ping a {@code /health} cada 10 minutos para que
     * el servicio gratuito no se duerma. Si esto devolviera 403 el job fallaria.
     */
    @Test
    void healthRespondeOkSinToken() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    /** La raiz tambien debe ser publica: es el Health Check Path por defecto de Render. */
    @Test
    void raizRespondeOkSinToken() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    /** El health check NO debe abrir el resto de la API: sigue exigiendo JWT. */
    @Test
    void healthPublicoNoAbreOtrasRutas() throws Exception {
        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isForbidden());
    }

    // ---------- LECTURA (GET) ----------

    @Test
    void docentePuedeLeerEdificios() throws Exception {
        mockMvc.perform(get("/api/edificios").with(user("docente").roles("DOCENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void encargadoPuedeLeerPisos() throws Exception {
        mockMvc.perform(get("/api/pisos/edificio/1").with(user("encargado").roles("ENCARGADO")))
                .andExpect(status().isOk());
    }

    @Test
    void sinTokenNoPuedeLeerEdificios() throws Exception {
        // Spring Security devuelve 403 (Http403ForbiddenEntryPoint) porque no
        // hay .exceptionHandling() configurado; no 401.
        mockMvc.perform(get("/api/edificios"))
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba decisiva: ¿matchea {@code /api/edificios/**} la ruta base
     * {@code /api/edificios}?
     *
     * <p>Se envía un código DUPLICADO (ED-04 ya existe) para que, en caso de
     * pasar la autorización, el servicio lance {@code IllegalArgumentException}
     * antes de guardar. Así no se crea ninguna fila en la BD bajo ninguna
     * de las dos ramas posibles:</p>
     * <ul>
     *   <li>si la regla matchea → 403 (DOCENTE sin rol ADMIN)</li>
     *   <li>si NO matchea → cae en anyRequest().authenticated() → 500</li>
     * </ul>
     */
    @Test
    void docenteNoPuedeCrearEdificiosEnRutaBase() throws Exception {
        mockMvc.perform(post("/api/edificios")
                        .with(user("docente").roles("DOCENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"ED-04\",\"nombre\":\"Duplicado\"}"))
                .andExpect(status().isForbidden());
    }

    /**
     * Mismo caso para {@code /api/pisos} con DOCENTE.
     */
    @Test
    void docenteNoPuedeCrearPisosEnRutaBase() throws Exception {
        mockMvc.perform(post("/api/pisos")
                        .with(user("docente").roles("DOCENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"edificioId\":1,\"numeroPiso\":1,\"nombre\":\"Duplicado\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- ESCRITURA (PUT/POST) ----------

    @Test
    void docenteNoPuedeModificarEdificios() throws Exception {
        mockMvc.perform(put("/api/edificios/999999")
                        .with(user("docente").roles("DOCENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_EDIFICIO))
                .andExpect(status().isForbidden());
    }

    @Test
    void encargadoNoPuedeModificarPisos() throws Exception {
        mockMvc.perform(put("/api/pisos/999999")
                        .with(user("encargado").roles("ENCARGADO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"edificioId\":1,\"numeroPiso\":99,\"nombre\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSiPuedeModificarEdificios() throws Exception {
        // 404 = pasó la autorización y llegó al servicio (id inexistente).
        // Si diera 403 significaría que la regla de rol no matchea la ruta base.
        mockMvc.perform(put("/api/edificios/999999")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_EDIFICIO))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminSiPuedeModificarPisos() throws Exception {
        mockMvc.perform(put("/api/pisos/999999")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"edificioId\":1,\"numeroPiso\":99,\"nombre\":\"X\"}"))
                .andExpect(status().isNotFound());
    }
}
