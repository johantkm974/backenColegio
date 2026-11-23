package com.example.demo.repository;

import com.example.demo.model.ReporteFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de fotos adjuntas de reportes (tabla reporte_fotos).
 */
@Repository
public interface ReporteFotoRepository extends JpaRepository<ReporteFoto, Long> {

    List<ReporteFoto> findByReporteIdOrderByCreadoEnAsc(Long reporteId);

    long countByReporteId(Long reporteId);
}
