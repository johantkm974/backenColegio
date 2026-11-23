package com.example.demo.repository;

import com.example.demo.model.Piso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de pisos.
 */
@Repository
public interface PisoRepository extends JpaRepository<Piso, Integer> {

    List<Piso> findByEdificio_Id(Integer edificioId);

    boolean existsByEdificio_IdAndNumeroPiso(Integer edificioId, Integer numeroPiso);
}
