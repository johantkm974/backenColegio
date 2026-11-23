package com.example.demo.repository;

import com.example.demo.model.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de ambientes.
 */
@Repository
public interface AmbienteRepository extends JpaRepository<Ambiente, Integer> {

    List<Ambiente> findByPiso_Id(Integer pisoId);

    List<Ambiente> findByPiso_IdAndTipo(Integer pisoId, String tipo);
}
