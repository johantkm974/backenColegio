package com.example.demo.controller;

import com.example.demo.dto.AmbienteRequest;
import com.example.demo.dto.AmbienteResponse;
import com.example.demo.service.AmbienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ambientes")
public class AmbienteController {

    private final AmbienteService ambienteService;

    public AmbienteController(AmbienteService ambienteService) {
        this.ambienteService = ambienteService;
    }

    @GetMapping
    public ResponseEntity<List<AmbienteResponse>> listarTodos() {
        return ResponseEntity.ok(ambienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmbienteResponse> buscarPorId(@PathVariable Integer id) {
        return ambienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/piso/{pisoId}")
    public ResponseEntity<List<AmbienteResponse>> listarPorPiso(@PathVariable Integer pisoId) {
        return ResponseEntity.ok(ambienteService.listarPorPiso(pisoId));
    }

    @PostMapping
    public ResponseEntity<AmbienteResponse> crear(@RequestBody AmbienteRequest request) {
        AmbienteResponse creado = ambienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmbienteResponse> actualizar(@PathVariable Integer id,
                                                       @RequestBody AmbienteRequest request) {
        return ambienteService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<AmbienteResponse> cambiarEstado(@PathVariable Integer id,
                                                          @RequestParam Boolean activo) {
        return ambienteService.cambiarEstado(id, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        ambienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
