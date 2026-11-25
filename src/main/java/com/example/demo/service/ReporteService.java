package com.example.demo.service;

import com.example.demo.dto.ReporteRequest;
import com.example.demo.dto.ReporteResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de reportes de daños.
 */
public interface ReporteService {

    /** Límite práctico de fotos por reporte. */
    int MAX_FOTOS = 5;

    List<ReporteResponse> listarTodos();

    Optional<ReporteResponse> buscarPorId(Long id);

    Optional<ReporteResponse> buscarPorCodigoTicket(String codigoTicket);

    List<ReporteResponse> listarPorEstado(String estado);

    List<ReporteResponse> listarPorDocente(Long docenteId);

    List<ReporteResponse> listarPorEncargado(Long encargadoId);

    ReporteResponse crear(ReporteRequest request, Long docenteId);

    ReporteResponse actualizar(Long id, ReporteRequest request, Long usuarioId);

    Optional<ReporteResponse> actualizarEstado(Long id, String nuevoEstado, String observacion, Long usuarioId);

    Optional<ReporteResponse> asignarEncargado(Long id, Long encargadoId);

    void eliminar(Long id);

    ReporteResponse subirFoto(Long id, MultipartFile foto, Long usuarioId);

    ReporteResponse subirFotos(Long id, List<MultipartFile> fotos, Long usuarioId);

    ReporteResponse eliminarFotoPorUrl(Long id, String url, Long usuarioId);
}
