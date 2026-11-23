package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta de reporte.
 */
public record ReporteResponse(
        Long id,
        String codigoTicket,
        Integer ambienteId,
        String ambienteNombre,
        String pisoNombre,
        String edificioNombre,
        Long docenteId,
        String docenteNombre,
        Long encargadoId,
        String encargadoNombre,
        String titulo,
        String descripcion,
        String fotoUrl,
        String prioridad,
        String estado,
        String observacionAtencion,
        LocalDateTime fechaReporte,
        LocalDateTime fechaAtencion,
        List<String> fotosUrls
) {
}
