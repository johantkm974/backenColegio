package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoint publico (sin JWT) para health checks.
 *
 * <p>Lo usan:</p>
 * <ul>
 *   <li>Render, para comprobar que la instancia esta viva (Health Check Path = /health)</li>
 *   <li>El cron job externo que hace ping cada 10 minutos para que el servicio
 *       gratuito de Render no se duerma por inactividad.</li>
 * </ul>
 *
 * <p>NO expone ningun dato sensible: solo estado y marca de tiempo.</p>
 */
@RestController
public class HealthController {

    @GetMapping({"/health", "/"})
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "reporta-api",
                "timestamp", Instant.now().toString()
        ));
    }
}
