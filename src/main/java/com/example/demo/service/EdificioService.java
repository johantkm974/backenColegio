package com.example.demo.service;

import com.example.demo.dto.EdificioRequest;
import com.example.demo.dto.EdificioResponse;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de edificios.
 */
public interface EdificioService {

    List<EdificioResponse> listarTodos();

    Optional<EdificioResponse> buscarPorId(Integer id);

    Optional<EdificioResponse> buscarPorCodigo(String codigo);

    EdificioResponse crear(EdificioRequest request);

    Optional<EdificioResponse> actualizar(Integer id, EdificioRequest request);

    Optional<EdificioResponse> cambiarEstado(Integer id, Boolean activo);

    void eliminar(Integer id);
}
