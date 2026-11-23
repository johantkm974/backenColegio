package com.example.demo.service;

import com.example.demo.dto.ReporteRequest;
import com.example.demo.dto.ReporteResponse;

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

    Optional<ReporteResponse> actualizarEstado(Long id, String nuevoEstado, String observacion, Long usuarioId);

    Optional<ReporteResponse> asignarEncargado(Long id, Long encargadoId);

    void eliminar(Long id);

    ReporteResponse subirFoto(Long id, org.springframework.web.multipart.MultipartFile foto);

    /**
     * Sube hasta {@link #MAX_FOTOS} fotos de un reporte a Cloudinary y las
     * registra en la tabla reporte_fotos.
     *
     * @throws IllegalArgumentException si la lista está vacía o supera el límite
     */
    ReporteResponse subirFotos(Long id, List<org.springframework.web.multipart.MultipartFile> fotos);
}
