package com.example.demo.repository;

import com.example.demo.model.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de edificios.
 */
@Repository
public interface EdificioRepository extends JpaRepository<Edificio, Integer> {

    Optional<Edificio> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
