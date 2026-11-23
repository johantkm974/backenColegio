package com.example.demo.repository;

import com.example.demo.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de reportes.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByEstado(String estado);

    List<Reporte> findByDocente_Id(Long docenteId);

    List<Reporte> findByEncargado_Id(Long encargadoId);

    List<Reporte> findByAmbiente_Id(Integer ambienteId);

    Optional<Reporte> findByCodigoTicket(String codigoTicket);

    boolean existsByCodigoTicket(String codigoTicket);
}
