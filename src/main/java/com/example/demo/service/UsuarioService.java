package com.example.demo.service;

import com.example.demo.dto.UsuarioRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de usuarios.
 */
public interface UsuarioService {

    List<UsuarioResponse> listarTodos();

    Optional<UsuarioResponse> buscarPorId(Long id);

    Optional<UsuarioResponse> buscarPorCorreo(String correo);

    UsuarioResponse crear(UsuarioRequest request);

    Optional<UsuarioResponse> actualizar(Long id, UsuarioRequest request);

    Optional<UsuarioResponse> cambiarEstado(Long id, Boolean activo);

    void eliminar(Long id);

    Usuario cargarEntidad(Long id);

    void cambiarContrasena(Long id, String nuevaPassword);
}
