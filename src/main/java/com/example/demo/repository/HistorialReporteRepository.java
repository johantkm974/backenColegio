package com.example.demo.repository;

import com.example.demo.model.HistorialReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de historial de reportes.
 */
@Repository
public interface HistorialReporteRepository extends JpaRepository<HistorialReporte, Long> {

    List<HistorialReporte> findByReporte_IdOrderByCreadoEnAsc(Long reporteId);
}
