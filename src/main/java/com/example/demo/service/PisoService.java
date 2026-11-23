package com.example.demo.service;

import com.example.demo.dto.PisoRequest;
import com.example.demo.dto.PisoResponse;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de pisos.
 */
public interface PisoService {

    List<PisoResponse> listarTodos();

    Optional<PisoResponse> buscarPorId(Integer id);

    List<PisoResponse> listarPorEdificio(Integer edificioId);

    PisoResponse crear(PisoRequest request);

    Optional<PisoResponse> actualizar(Integer id, PisoRequest request);

    Optional<PisoResponse> cambiarEstado(Integer id, Boolean activo);

    void eliminar(Integer id);
}
