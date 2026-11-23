package com.example.demo.controller;

import com.example.demo.dto.EdificioRequest;
import com.example.demo.dto.EdificioResponse;
import com.example.demo.service.EdificioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/edificios")
public class EdificioController {

    private final EdificioService edificioService;

    public EdificioController(EdificioService edificioService) {
        this.edificioService = edificioService;
    }

    @GetMapping
    public ResponseEntity<List<EdificioResponse>> listarTodos() {
        return ResponseEntity.ok(edificioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EdificioResponse> buscarPorId(@PathVariable Integer id) {
        return edificioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EdificioResponse> buscarPorCodigo(@PathVariable String codigo) {
        return edificioService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EdificioResponse> crear(@RequestBody EdificioRequest request) {
        EdificioResponse creado = edificioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EdificioResponse> actualizar(@PathVariable Integer id,
                                                       @RequestBody EdificioRequest request) {
        return edificioService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EdificioResponse> cambiarEstado(@PathVariable Integer id,
                                                          @RequestParam Boolean activo) {
        return edificioService.cambiarEstado(id, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        edificioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
