package com.example.demo.service;

import com.example.demo.dto.AmbienteRequest;
import com.example.demo.dto.AmbienteResponse;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de ambientes.
 */
public interface AmbienteService {

    List<AmbienteResponse> listarTodos();

    Optional<AmbienteResponse> buscarPorId(Integer id);

    List<AmbienteResponse> listarPorPiso(Integer pisoId);

    AmbienteResponse crear(AmbienteRequest request);

    Optional<AmbienteResponse> actualizar(Integer id, AmbienteRequest request);

    Optional<AmbienteResponse> cambiarEstado(Integer id, Boolean activo);

    void eliminar(Integer id);
}
