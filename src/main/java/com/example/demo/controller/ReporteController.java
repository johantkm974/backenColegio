package com.example.demo.controller;

import com.example.demo.dto.ReporteRequest;
import com.example.demo.dto.ReporteResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ResponseEntity<List<ReporteResponse>> listarTodos() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponse> buscarPorId(@PathVariable Long id) {
        return reporteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigoTicket}")
    public ResponseEntity<ReporteResponse> buscarPorCodigo(@PathVariable String codigoTicket) {
        return reporteService.buscarPorCodigoTicket(codigoTicket)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReporteResponse>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(reporteService.listarPorEstado(estado));
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<ReporteResponse>> listarPorDocente(@PathVariable Long docenteId) {
        return ResponseEntity.ok(reporteService.listarPorDocente(docenteId));
    }

    @GetMapping("/encargado/{encargadoId}")
    public ResponseEntity<List<ReporteResponse>> listarPorEncargado(@PathVariable Long encargadoId) {
        return ResponseEntity.ok(reporteService.listarPorEncargado(encargadoId));
    }

    @PostMapping
    public ResponseEntity<ReporteResponse> crear(@Valid @RequestBody ReporteRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long docenteId = userDetails.getUsuario().getId();
        ReporteResponse creado = reporteService.crear(request, docenteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteResponse> subirFoto(
            @PathVariable Long id,
            @RequestParam("foto") MultipartFile foto) {
        ReporteResponse actualizado = reporteService.subirFoto(id, foto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Sube de 1 a {@code ReporteService.MAX_FOTOS} fotos en una sola petición
     * (nombre de campo "fotos"). Sube a Cloudinary y las registra en reporte_fotos.
     */
    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteResponse> subirFotos(
            @PathVariable Long id,
            @RequestParam("fotos") List<MultipartFile> fotos) {
        ReporteResponse actualizado = reporteService.subirFotos(id, fotos);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReporteResponse> actualizarEstado(@PathVariable Long id,
                                                            @RequestParam String nuevoEstado,
                                                            @RequestParam(required = false) String observacion,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long usuarioId = userDetails.getUsuario().getId();
        return reporteService.actualizarEstado(id, nuevoEstado, observacion, usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/encargado/{encargadoId}")
    public ResponseEntity<ReporteResponse> asignarEncargado(@PathVariable Long id,
                                                            @PathVariable Long encargadoId) {
        return reporteService.asignarEncargado(id, encargadoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
