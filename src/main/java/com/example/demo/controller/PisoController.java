package com.example.demo.controller;

import com.example.demo.dto.PisoRequest;
import com.example.demo.dto.PisoResponse;
import com.example.demo.service.PisoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pisos")
public class PisoController {

    private final PisoService pisoService;

    public PisoController(PisoService pisoService) {
        this.pisoService = pisoService;
    }

    @GetMapping
    public ResponseEntity<List<PisoResponse>> listarTodos() {
        return ResponseEntity.ok(pisoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PisoResponse> buscarPorId(@PathVariable Integer id) {
        return pisoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/edificio/{edificioId}")
    public ResponseEntity<List<PisoResponse>> listarPorEdificio(@PathVariable Integer edificioId) {
        return ResponseEntity.ok(pisoService.listarPorEdificio(edificioId));
    }

    @PostMapping
    public ResponseEntity<PisoResponse> crear(@RequestBody PisoRequest request) {
        PisoResponse creado = pisoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PisoResponse> actualizar(@PathVariable Integer id,
                                                   @RequestBody PisoRequest request) {
        return pisoService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PisoResponse> cambiarEstado(@PathVariable Integer id,
                                                      @RequestParam Boolean activo) {
        return pisoService.cambiarEstado(id, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        pisoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
